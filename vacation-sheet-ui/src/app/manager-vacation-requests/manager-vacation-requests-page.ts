import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { ProjectsStore } from '../projects/projects.store';
import { VacationRequestState, VacationType } from '../vacation-requests/vacation-requests.store';
import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';
import {
  ManagerSortField,
  ManagerVacationRequestsViewStore,
} from './manager-vacation-requests-view.store';

@Component({
  selector: 'app-manager-vacation-requests-page',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './manager-vacation-requests-page.html',
  styleUrl: './manager-vacation-requests-page.scss',
})
export class ManagerVacationRequestsPage implements OnInit {
  protected readonly store = inject(ManagerVacationRequestsStore);
  protected readonly view = inject(ManagerVacationRequestsViewStore);
  protected readonly projectsStore = inject(ProjectsStore);
  protected readonly pendingStates = signal<Record<number, ManagerVacationRequestState>>({});
  protected readonly columns = [
    'title',
    'email',
    'firstName',
    'lastName',
    'projects',
    'ctime',
    'startDate',
    'endDate',
    'requestState',
    'vacationType',
    'quickReview',
    'review',
  ];

  ngOnInit(): void {
    this.store.load();
    this.projectsStore.load();
  }

  protected selectedState(item: ManagerVacationRequest): ManagerVacationRequestState {
    return (
      this.pendingStates()[item.request.id] ??
      (item.request.requestState as ManagerVacationRequestState)
    );
  }

  protected setSelectedState(id: number, state: ManagerVacationRequestState): void {
    this.pendingStates.update((states) => ({ ...states, [id]: state }));
  }

  protected saveState(item: ManagerVacationRequest): void {
    this.store.review(
      item.request.id,
      {
        managerComment: null,
        updateManagerComment: false,
        requestState: this.selectedState(item),
      },
      (updated) =>
        this.setSelectedState(
          updated.request.id,
          updated.request.requestState as ManagerVacationRequestState,
        ),
    );
  }

  protected changeSort(sort: Sort): void {
    if (!sort.direction) {
      this.view.setSort('ctime', 'desc');
    } else {
      this.view.setSort(sort.active as ManagerSortField, sort.direction);
    }
  }

  protected changePage(page: PageEvent): void {
    this.view.setPage(page.pageIndex, page.pageSize);
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
}
