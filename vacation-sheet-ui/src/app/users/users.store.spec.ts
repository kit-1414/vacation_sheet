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

    http.expectOne('/api/users').flush([
      {
        id: 'user-1',
        email: 'user@example.com',
        displayName: 'Test User',
        ctime: '2026-08-05T00:00:00Z',
        utime: '2026-08-05T00:00:00Z',
      },
    ]);

    expect(store.users()).toHaveLength(1);
    expect(store.loading()).toBe(false);
  });
});
