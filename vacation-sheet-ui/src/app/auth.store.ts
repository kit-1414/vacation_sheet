import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, ReplaySubject, take } from 'rxjs';

export type UserRole = 'NOBODY' | 'ADMIN' | 'MANAGER' | 'USER';

export interface CurrentUser {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
  isAdmin: boolean;
  isActive: boolean;
  ctime: string | null;
  utime: string | null;
  roles: UserRole[];
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly http = inject(HttpClient);

  readonly user = signal<CurrentUser | null>(null);
  readonly loading = signal(true);
  private readonly loaded = new ReplaySubject<CurrentUser | null>(1);
  private loadStarted = false;

  load(): void {
    if (this.loadStarted) return;
    this.loadStarted = true;
    this.http.get<CurrentUser>('/api/auth/me').subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
        this.loaded.next(user);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 403) {
          window.location.assign('/login');
          return;
        }
        this.user.set(null);
        this.loading.set(false);
        this.loaded.next(null);
      },
    });
  }

  ensureLoaded(): Observable<CurrentUser | null> {
    this.load();
    return this.loaded.pipe(take(1));
  }

  hasRole(role: UserRole): boolean {
    return this.user()?.roles.includes(role) ?? false;
  }

  canAdminister(): boolean {
    return this.hasRole('ADMIN');
  }

  canManageRelations(): boolean {
    return this.canAdminister() || this.hasRole('MANAGER');
  }

  logout(): void {
    this.http.post<void>('/api/auth/logout', {}).subscribe(() => this.user.set(null));
  }
}
