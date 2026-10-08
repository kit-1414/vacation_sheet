import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ManagerVacationRequest, ManagerVacationRequestsStore } from './manager-vacation-requests.store';

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
      author: { id: 1, email: 'user@example.com', firstName: 'Test', lastName: 'User' },
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

  it('loads all vacation requests', () => {
    store.load();

    const request = http.expectOne('/api/vacation_request');
    expect(request.request.method).toBe('GET');
    request.flush([item]);

    expect(store.requests()).toEqual([item]);
    expect(store.loading()).toBe(false);
  });

  it('replaces a request while preserving author projects', () => {
    store.requests.set([item]);
    const updatedRequest = { ...item.request, title: 'Updated vacation' };

    store.replaceRequest(updatedRequest);

    expect(store.requests()).toEqual([{ ...item, request: updatedRequest }]);
  });
});
