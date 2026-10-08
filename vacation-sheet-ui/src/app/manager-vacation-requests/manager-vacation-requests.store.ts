import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';

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

@Injectable({ providedIn: 'root' })
export class ManagerVacationRequestsStore {
  private readonly http = inject(HttpClient);
  private readonly listApiUrl = '/api/vacation_request';

  readonly requests = signal<ManagerVacationRequest[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<ManagerVacationRequest[]>(this.listApiUrl).subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => this.fail('Не удалось загрузить заявления на отпуск'),
    });
  }

  replaceRequest(request: VacationRequest): void {
    this.requests.update((requests) =>
      requests.map((item) =>
        item.request.id === request.id ? { ...item, request } : item,
      ),
    );
  }

  private fail(message: string): void {
    this.error.set(message);
    this.loading.set(false);
  }
}
