import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';

export interface CurrentUser {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  ctime: string | null;
  utime: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly http = inject(HttpClient);

  readonly user = signal<CurrentUser | null>(null);
  readonly loading = signal(true);

  load(): void {
    this.http.get<CurrentUser>('/api/auth/me').subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 403) {
          window.location.assign('/login');
          return;
        }
        this.user.set(null);
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.http.post<void>('/api/auth/logout', {}).subscribe(() => this.user.set(null));
  }
}
