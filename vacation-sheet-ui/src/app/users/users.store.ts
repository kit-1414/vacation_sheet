import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';

export interface UserAccount {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
  ctime: string | null;
  utime: string | null;
}

@Injectable({ providedIn: 'root' })
export class UsersStore {
  private readonly http = inject(HttpClient);

  readonly users = signal<UserAccount[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<UserAccount[]>('/api/users').subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Не удалось загрузить пользователей');
        this.loading.set(false);
      },
    });
  }
}
