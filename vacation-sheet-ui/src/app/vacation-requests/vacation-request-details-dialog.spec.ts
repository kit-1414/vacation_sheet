import { signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ManagerVacationRequest } from '../manager-vacation-requests/manager-vacation-requests.store';
import { VacationRequestDetailsDialog } from './vacation-request-details-dialog';
import { VacationRequestsStore } from './vacation-requests.store';

describe('VacationRequestDetailsDialog', () => {
  it('disables the form in view mode', async () => {
    const item: ManagerVacationRequest = {
      request: {
        id: 10,
        title: 'Vacation',
        requestState: 'APPROVED',
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
    await TestBed.configureTestingModule({
      imports: [VacationRequestDetailsDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { item, editable: false } },
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        {
          provide: VacationRequestsStore,
          useValue: { saving: signal(false), error: signal<string | null>(null) },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(VacationRequestDetailsDialog);
    const component = fixture.componentInstance as unknown as { form: FormGroup };

    expect(component.form.disabled).toBe(true);
  });
});
