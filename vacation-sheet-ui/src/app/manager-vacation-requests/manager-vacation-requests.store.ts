import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

import {
  VacationRequest,
  VacationRequestState,
} from '../vacation-requests/vacation-requests.store';

export type ManagerVacationRequestState = Exclude<VacationRequestState, 'DRAFT'>;

export interface ProjectSummary {
  id: number;
  name: string;
}

export interface ManagerVacationRequest {
  request: VacationRequest;
  authorProjects: ProjectSummary[];
}

export interface ManagerReviewPayload {
  managerComment: string | null;
  updateManagerComment: boolean;
  requestState: ManagerVacationRequestState;
}

@Injectable({ providedIn: 'root' })
export class ManagerVacationRequestsStore {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/manager/actions/vacation_request';

  readonly requests = signal<ManagerVacationRequest[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<ManagerVacationRequest[]>(this.apiUrl).subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => this.fail('Не удалось загрузить заявления на рассмотрение'),
    });
  }

  loadOne(id: number): Observable<ManagerVacationRequest> {
    return this.http.get<ManagerVacationRequest>(`${this.apiUrl}/${id}`);
  }

  review(
    id: number,
    payload: ManagerReviewPayload,
    onSuccess?: (request: ManagerVacationRequest) => void,
  ): void {
    this.saving.set(true);
    this.error.set(null);
    this.http.put<ManagerVacationRequest>(`${this.apiUrl}/${id}`, payload).subscribe({
      next: (request) => {
        this.requests.update((requests) =>
          requests.map((item) => (item.request.id === request.request.id ? request : item)),
        );
        this.saving.set(false);
        onSuccess?.(request);
      },
      error: () => this.fail('Не удалось сохранить результат рассмотрения'),
    });
  }

  private fail(message: string): void {
    this.error.set(message);
    this.loading.set(false);
    this.saving.set(false);
  }
}
