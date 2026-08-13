import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { UsersStore } from './users.store';

describe('UsersStore', () => {
  let store: UsersStore;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(UsersStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads registered users', () => {
    store.load();

    const request = http.expectOne('/api/users');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 1,
        email: 'user@example.com',
        firstName: 'Test',
        lastName: 'User',
        isAdmin: true,
        isActive: false,
        ctime: '2026-08-05T00:00:00Z',
        utime: '2026-08-05T00:00:00Z',
      },
    ]);

    expect(store.users()).toHaveLength(1);
    expect(store.users()[0]).toMatchObject({
      email: 'user@example.com',
      firstName: 'Test',
      lastName: 'User',
      isAdmin: true,
      isActive: false,
    });
    expect(store.loading()).toBe(false);
  });

  it('creates a user', () => {
    const onSuccess = vi.fn();
    const body = { email: 'user@example.com', firstName: 'Test', lastName: 'User', isAdmin: false, isActive: true };
    store.create(body, onSuccess);

    const request = http.expectOne('/api/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush({
      id: 1,
      email: 'user@example.com',
      firstName: 'Test',
      lastName: 'User',
      isAdmin: false,
      isActive: true,
      ctime: null,
      utime: null,
    });

    expect(store.users()).toHaveLength(1);
    expect(onSuccess).toHaveBeenCalledOnce();
  });

  it('updates account flags', () => {
    const onSuccess = vi.fn();
    const body = { email: 'user@example.com', firstName: 'Test', lastName: 'User', isAdmin: true, isActive: false };
    store.update(1, body, onSuccess);

    const request = http.expectOne('/api/users/1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush({ id: 1, ...body, ctime: null, utime: null });

    expect(onSuccess).toHaveBeenCalledOnce();
  });

  it('deletes a user', () => {
    store.delete(1);

    const request = http.expectOne('/api/users/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
