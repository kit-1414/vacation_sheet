import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { AuthStore, UserRole } from './auth.store';

function roleGuard(roles: UserRole[], redirectTo: string): CanActivateFn {
  return () => {
    const auth = inject(AuthStore);
    const router = inject(Router);
    return auth.ensureLoaded().pipe(
      map((user) => user && (user.roles.includes('ADMIN') || roles.some((role) => user.roles.includes(role)))
        ? true
        : router.createUrlTree([redirectTo])),
    );
  };
}

export const adminGuard = roleGuard(['ADMIN'], '/projects');
export const managerGuard = roleGuard(['MANAGER'], '/projects');
