import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import {
  ManagerReviewPayload,
  ManagerVacationRequest,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';

describe('ManagerVacationRequestsStore', () => {
  let store: ManagerVacationRequestsStore;
  let http: HttpTestingController;

  const item: ManagerVacationRequest = {
    request: {
      id: 10,
      title: 'Vacation',
      requestState: 'READY',
      vacationType: 'PAYMENT_VACATION',
      startDate: '2026-09-10',
      endDate: '2026-09-20',
      userComments: 'Rest',
      managerComments: null,
      author: { id: 1, email: 'user@example.com', firstName: 'Test', lastName: 'User' },
      manager: null,
      ctime: '2026-09-01T10:00:00Z',
      utime: '2026-09-01T10:00:00Z',
    },
    authorProjects: [{ id: 3, name: 'Vacation Sheet' }],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(ManagerVacationRequestsStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads vacation requests for review', () => {
    store.load();

    const request = http.expectOne('/api/manager/actions/vacation_request');
    expect(request.request.method).toBe('GET');
    request.flush([item]);

    expect(store.requests()).toEqual([item]);
    expect(store.loading()).toBe(false);
  });

  it('loads one vacation request for review', () => {
    let response: ManagerVacationRequest | undefined;
    store.loadOne(10).subscribe((item) => (response = item));

    const request = http.expectOne('/api/manager/actions/vacation_request/10');
    expect(request.request.method).toBe('GET');
    request.flush(item);

    expect(response).toEqual(item);
  });

  it('reviews a request and updates the list', () => {
    const onSuccess = vi.fn();
    const payload: ManagerReviewPayload = {
      managerComment: 'Approved',
      updateManagerComment: true,
      requestState: 'APPROVED',
    };
    const updated: ManagerVacationRequest = {
      ...item,
      request: { ...item.request, requestState: 'APPROVED', managerComments: 'Approved' },
    };
    store.requests.set([item]);
    store.review(10, payload, onSuccess);

    const request = http.expectOne('/api/manager/actions/vacation_request/10');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush(updated);

    expect(store.requests()).toEqual([updated]);
    expect(onSuccess).toHaveBeenCalledWith(updated);
  });
});
