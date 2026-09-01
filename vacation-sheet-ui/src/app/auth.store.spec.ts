import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthStore, CurrentUser } from './auth.store';

describe('AuthStore', () => {
  let store: AuthStore;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(AuthStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('uses roles from the current user response', () => {
    store.load();
    const user: CurrentUser = {
      id: 1,
      email: 'admin@example.com',
      firstName: null,
      lastName: null,
      isAdmin: true,
      isActive: true,
      ctime: null,
      utime: null,
      roles: ['ADMIN', 'USER'],
    };
    http.expectOne('/api/auth/me').flush(user);

    expect(store.canAdminister()).toBe(true);
    expect(store.canManageRelations()).toBe(true);
    expect(store.canManageVacationRequests()).toBe(true);
    expect(store.canReviewVacationRequests()).toBe(true);
  });

  it('allows managers to manage relations but not administer', () => {
    store.load();
    http.expectOne('/api/auth/me').flush({
      id: 2,
      email: 'manager@example.com',
      firstName: null,
      lastName: null,
      isAdmin: false,
      isActive: true,
      ctime: null,
      utime: null,
      roles: ['MANAGER'],
    });

    expect(store.canAdminister()).toBe(false);
    expect(store.canManageRelations()).toBe(true);
    expect(store.canManageVacationRequests()).toBe(false);
    expect(store.canReviewVacationRequests()).toBe(true);
  });
});
