import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { Observable, firstValueFrom, of } from 'rxjs';
import { CanActivateFn } from '@angular/router';

import { AuthStore, CurrentUser, UserRole } from './auth.store';
import { managerGuard, userGuard } from './auth.guard';

describe('userGuard', () => {
  async function runGuard(guard: CanActivateFn, roles: UserRole[]): Promise<boolean | UrlTree> {
    const user: CurrentUser = {
      id: 1,
      email: 'user@example.com',
      firstName: null,
      lastName: null,
      isAdmin: roles.includes('ADMIN'),
      isActive: roles.includes('USER'),
      ctime: null,
      utime: null,
      roles,
    };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthStore, useValue: { ensureLoaded: () => of(user) } },
      ],
    });
    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
    return firstValueFrom(result as Observable<boolean | UrlTree>);
  }

  afterEach(() => TestBed.resetTestingModule());

  it('allows users to edit vacation requests', async () => {
    expect(await runGuard(userGuard, ['USER'])).toBe(true);
  });

  it('redirects inactive users to the vacation request list', async () => {
    const result = await runGuard(userGuard, ['NOBODY']);

    expect(result).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe(
      '/profile/vacation-requests',
    );
  });

  it('allows managers to open vacation request reviews', async () => {
    expect(await runGuard(managerGuard, ['MANAGER'])).toBe(true);
  });
});
