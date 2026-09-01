import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import {
  VacationRequest,
  VacationRequestPayload,
  VacationRequestsStore,
} from './vacation-requests.store';

describe('VacationRequestsStore', () => {
  let store: VacationRequestsStore;
  let http: HttpTestingController;

  const payload: VacationRequestPayload = {
    title: 'Летний отпуск',
    requestState: 'DRAFT',
    vacationType: 'PAYMENT_VACATION',
    startDate: '2026-09-10',
    endDate: '2026-09-20',
    userComments: null,
  };
  const vacationRequest: VacationRequest = {
    id: 1,
    ...payload,
    managerComments: null,
    author: { id: 2, email: 'user@example.com', firstName: 'Test', lastName: 'User' },
    manager: null,
    ctime: '2026-09-01T10:00:00Z',
    utime: '2026-09-01T10:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(VacationRequestsStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads current user vacation requests', () => {
    store.load();

    const request = http.expectOne('/api/user/actions/vacation_request');
    expect(request.request.method).toBe('GET');
    request.flush([vacationRequest]);

    expect(store.requests()).toEqual([vacationRequest]);
    expect(store.loading()).toBe(false);
  });

  it('creates a vacation request and calls success callback', () => {
    const onSuccess = vi.fn();
    store.create(payload, onSuccess);

    const request = http.expectOne('/api/user/actions/vacation_request');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(vacationRequest);

    expect(store.requests()).toEqual([vacationRequest]);
    expect(onSuccess).toHaveBeenCalledOnce();
  });

  it('updates a vacation request', () => {
    const onSuccess = vi.fn();
    store.requests.set([vacationRequest]);
    const updated = {
      ...vacationRequest,
      title: 'Обновлённый отпуск',
      requestState: 'READY' as const,
    };
    store.update(1, { ...payload, title: updated.title, requestState: 'READY' }, onSuccess);

    const request = http.expectOne('/api/user/actions/vacation_request/1');
    expect(request.request.method).toBe('PUT');
    request.flush(updated);

    expect(store.requests()).toEqual([updated]);
    expect(onSuccess).toHaveBeenCalledOnce();
  });

  it('deletes a vacation request', () => {
    store.requests.set([vacationRequest]);
    store.delete(1);

    const request = http.expectOne('/api/user/actions/vacation_request/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    expect(store.requests()).toEqual([]);
  });
});
