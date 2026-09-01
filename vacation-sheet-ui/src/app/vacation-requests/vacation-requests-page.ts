import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { AuthStore } from '../auth.store';
import {
  VacationRequest,
  VacationRequestState,
  VacationRequestsStore,
  VacationType,
} from './vacation-requests.store';

type SortField = 'ctime' | 'startDate' | 'endDate';
type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-vacation-requests-page',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './vacation-requests-page.html',
  styleUrl: './vacation-requests-page.scss',
})
export class VacationRequestsPage implements OnInit {
  protected readonly auth = inject(AuthStore);
  protected readonly store = inject(VacationRequestsStore);
  protected readonly stateFilter = signal<VacationRequestState | ''>('');
  protected readonly typeFilter = signal<VacationType | ''>('');
  protected readonly sortField = signal<SortField>('ctime');
  protected readonly sortDirection = signal<SortDirection>('desc');
  protected readonly columns = [
    'title',
    'ctime',
    'requestState',
    'vacationType',
    'startDate',
    'endDate',
    'edit',
    'delete',
  ];

  protected readonly visibleRequests = computed(() => {
    const state = this.stateFilter();
    const type = this.typeFilter();
    const field = this.sortField();
    const direction = this.sortDirection() === 'asc' ? 1 : -1;
    return [...this.store.requests()]
      .filter(
        (request) =>
          (!state || request.requestState === state) && (!type || request.vacationType === type),
      )
      .sort(
        (left, right) =>
          this.sortValue(left, field).localeCompare(this.sortValue(right, field)) * direction,
      );
  });

  ngOnInit(): void {
    this.store.load();
  }

  protected canModify(request: VacationRequest): boolean {
    return (
      this.auth.canManageVacationRequests() &&
      (request.requestState === 'DRAFT' || request.requestState === 'READY')
    );
  }

  protected delete(request: VacationRequest): void {
    if (this.canModify(request)) this.store.delete(request.id);
  }

  protected stateLabel(state: VacationRequestState): string {
    return {
      DRAFT: 'Черновик',
      READY: 'Готово к согласованию',
      APPROVED: 'Одобрено',
      REJECTED: 'Отклонено',
    }[state];
  }

  protected typeLabel(type: VacationType): string {
    return type === 'PAYMENT_VACATION' ? 'Оплачиваемый отпуск' : 'За свой счёт';
  }

  protected dateLabel(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}.${month}.${year}`;
  }

  private sortValue(request: VacationRequest, field: SortField): string {
    return request[field] ?? '';
  }
}
