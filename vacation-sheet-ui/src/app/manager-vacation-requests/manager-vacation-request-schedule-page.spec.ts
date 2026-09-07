import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideRouter } from '@angular/router';

import { AuthStore } from '../auth.store';
import { ManagerVacationRequestSchedulePage } from './manager-vacation-request-schedule-page';
import {
  ManagerVacationRequest,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';
import { ManagerVacationRequestsViewStore } from './manager-vacation-requests-view.store';

describe('ManagerVacationRequestSchedulePage', () => {
  it('allows editing only for an owned ready request', () => {
    const item: ManagerVacationRequest = {
      request: {
        id: 10,
        title: 'Vacation',
        requestState: 'READY',
        vacationType: 'PAYMENT_VACATION',
        startDate: '2026-09-10',
        endDate: '2026-09-20',
        userComments: null,
        managerComments: null,
        author: { id: 1, email: 'user@example.com', firstName: null, lastName: null },
        manager: null,
        ctime: null,
        utime: null,
      },
      authorProjects: [],
    };
    TestBed.configureTestingModule({
      imports: [ManagerVacationRequestSchedulePage],
      providers: [
        provideRouter([]),
        { provide: MatDialog, useValue: { open: vi.fn() } },
        {
          provide: AuthStore,
          useValue: {
            user: signal({ id: 1 }),
            canManageVacationRequests: () => true,
            canReviewVacationRequests: () => false,
          },
        },
        {
          provide: ManagerVacationRequestsStore,
          useValue: { requests: signal([]), loading: signal(false), error: signal(null), load: vi.fn() },
        },
        { provide: ManagerVacationRequestsViewStore, useValue: { visibleRequests: signal([]) } },
      ],
    });
    const fixture = TestBed.createComponent(ManagerVacationRequestSchedulePage);
    const component = fixture.componentInstance as unknown as {
      canEdit: (value: ManagerVacationRequest) => boolean;
    };

    expect(component.canEdit(item)).toBe(true);
    expect(
      component.canEdit({ ...item, request: { ...item.request, requestState: 'APPROVED' } }),
    ).toBe(false);
    expect(
      component.canEdit({
        ...item,
        request: { ...item.request, author: { ...item.request.author, id: 2 } },
      }),
    ).toBe(false);
  });
});
