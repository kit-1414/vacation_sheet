import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

export interface UserAccount {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
  ctime: string | null;
  utime: string | null;
}

export interface UserAccountRequest {
  email: string;
  firstName: string | null;
  lastName: string | null;
}

@Injectable({ providedIn: 'root' })
export class UsersStore {
  private readonly http = inject(HttpClient);

  readonly users = signal<UserAccount[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
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

  loadOne(id: number): Observable<UserAccount> {
    return this.http.get<UserAccount>(`/api/users/${id}`);
  }

  create(request: UserAccountRequest, onSuccess: () => void): void {
    this.saving.set(true);
    this.error.set(null);
    this.http.post<UserAccount>('/api/users', request).subscribe({
      next: (user) => {
        this.users.update((users) => [...users, user]);
        this.saving.set(false);
        onSuccess();
      },
      error: () => this.fail('Не удалось создать пользователя'),
    });
  }

  update(id: number, request: UserAccountRequest, onSuccess: () => void): void {
    this.saving.set(true);
    this.error.set(null);
    this.http.put<UserAccount>(`/api/users/${id}`, request).subscribe({
      next: (user) => {
        this.users.update((users) => users.map((item) => item.id === user.id ? user : item));
        this.saving.set(false);
        onSuccess();
      },
      error: () => this.fail('Не удалось обновить пользователя'),
    });
  }

  private fail(message: string): void {
    this.error.set(message);
    this.loading.set(false);
    this.saving.set(false);
  }
}
