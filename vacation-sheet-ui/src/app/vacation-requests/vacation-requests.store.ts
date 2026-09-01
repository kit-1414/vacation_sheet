import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

export type VacationRequestState = 'DRAFT' | 'READY' | 'APPROVED' | 'REJECTED';
export type VacationType = 'PAYMENT_VACATION' | 'FREE_VACATION';

export interface VacationRequestUser {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
}

export interface VacationRequest {
  id: number;
  title: string;
  requestState: VacationRequestState;
  vacationType: VacationType;
  startDate: string;
  endDate: string;
  userComments: string | null;
  managerComments: string | null;
  author: VacationRequestUser;
  manager: VacationRequestUser | null;
  ctime: string | null;
  utime: string | null;
}

export interface VacationRequestPayload {
  title: string;
  requestState: Extract<VacationRequestState, 'DRAFT' | 'READY'>;
  vacationType: VacationType;
  startDate: string;
  endDate: string;
  userComments: string | null;
}

@Injectable({ providedIn: 'root' })
export class VacationRequestsStore {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/user/actions/vacation_request';

  readonly requests = signal<VacationRequest[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<VacationRequest[]>(this.apiUrl).subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => this.fail('Не удалось загрузить заявления на отпуск'),
    });
  }

  loadOne(id: number): Observable<VacationRequest> {
    return this.http.get<VacationRequest>(`${this.apiUrl}/${id}`);
  }

  create(payload: VacationRequestPayload, onSuccess: () => void): void {
    this.saving.set(true);
    this.error.set(null);
    this.http.post<VacationRequest>(this.apiUrl, payload).subscribe({
      next: (request) => {
        this.requests.update((requests) => [...requests, request]);
        this.saving.set(false);
        onSuccess();
      },
      error: () => this.fail('Не удалось создать заявление'),
    });
  }

  update(id: number, payload: VacationRequestPayload, onSuccess: () => void): void {
    this.saving.set(true);
    this.error.set(null);
    this.http.put<VacationRequest>(`${this.apiUrl}/${id}`, payload).subscribe({
      next: (request) => {
        this.requests.update((requests) =>
          requests.map((item) => (item.id === request.id ? request : item)),
        );
        this.saving.set(false);
        onSuccess();
      },
      error: () => this.fail('Не удалось обновить заявление'),
    });
  }

  delete(id: number): void {
    this.error.set(null);
    this.http.delete<void>(`${this.apiUrl}/${id}`).subscribe({
      next: () =>
        this.requests.update((requests) => requests.filter((request) => request.id !== id)),
      error: () => this.fail('Не удалось удалить заявление'),
    });
  }

  private fail(message: string): void {
    this.error.set(message);
    this.loading.set(false);
    this.saving.set(false);
  }
}
