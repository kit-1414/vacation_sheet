import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';

import { ManagerVacationRequestReviewPage } from './manager-vacation-request-review-page';
import { ManagerVacationRequest } from './manager-vacation-requests.store';

describe('ManagerVacationRequestReviewPage', () => {
  it('submits an empty manager comment as null', async () => {
    await TestBed.configureTestingModule({
      imports: [ManagerVacationRequestReviewPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '10' } } } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(ManagerVacationRequestReviewPage);
    const http = TestBed.inject(HttpTestingController);
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const item: ManagerVacationRequest = {
      request: {
        id: 10,
        title: 'Vacation',
        requestState: 'READY',
        vacationType: 'PAYMENT_VACATION',
        startDate: '2026-09-10',
        endDate: '2026-09-20',
        userComments: null,
        managerComments: 'Old comment',
        author: { id: 1, email: 'user@example.com', firstName: null, lastName: null },
        manager: null,
        ctime: null,
        utime: null,
      },
      authorProjects: [],
    };

    fixture.detectChanges();
    http.expectOne('/api/manager/actions/vacation_request/10').flush(item);
    fixture.detectChanges();

    const component = fixture.componentInstance as unknown as {
      form: FormGroup;
      submit: () => void;
    };
    component.form.setValue({ requestState: 'REJECTED', managerComment: '' });
    component.submit();

    const request = http.expectOne('/api/manager/actions/vacation_request/10');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      managerComment: null,
      updateManagerComment: true,
      requestState: 'REJECTED',
    });
    request.flush({
      ...item,
      request: { ...item.request, requestState: 'REJECTED', managerComments: null },
    });
    http.verify();
  });
});
