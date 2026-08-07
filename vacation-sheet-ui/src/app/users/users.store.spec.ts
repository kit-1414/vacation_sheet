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
        ctime: '2026-08-05T00:00:00Z',
        utime: '2026-08-05T00:00:00Z',
      },
    ]);

    expect(store.users()).toHaveLength(1);
    expect(store.users()[0]).toMatchObject({
      email: 'user@example.com',
      firstName: 'Test',
      lastName: 'User',
    });
    expect(store.loading()).toBe(false);
  });

  it('creates a user', () => {
    const onSuccess = vi.fn();
    store.create({ email: 'user@example.com', firstName: 'Test', lastName: 'User' }, onSuccess);

    const request = http.expectOne('/api/users');
    expect(request.request.method).toBe('POST');
    request.flush({
      id: 1,
      email: 'user@example.com',
      firstName: 'Test',
      lastName: 'User',
      ctime: null,
      utime: null,
    });

    expect(store.users()).toHaveLength(1);
    expect(onSuccess).toHaveBeenCalledOnce();
  });
});
