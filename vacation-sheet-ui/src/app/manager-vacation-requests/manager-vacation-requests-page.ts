import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort, SortDirection } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { ProjectsStore } from '../projects/projects.store';
import { VacationRequestState, VacationType } from '../vacation-requests/vacation-requests.store';
import { filterManagerVacationRequests } from './manager-vacation-request-filter';
import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';

type SortField =
  'email' | 'firstName' | 'lastName' | 'ctime' | 'startDate' | 'endDate' | 'requestState';

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
  protected readonly projectsStore = inject(ProjectsStore);
  protected readonly emailFilter = signal('');
  protected readonly firstNameFilter = signal('');
  protected readonly lastNameFilter = signal('');
  protected readonly stateFilter = signal<ManagerVacationRequestState[]>(['READY', 'APPROVED']);
  protected readonly periodStart = signal('');
  protected readonly periodEnd = signal('');
  protected readonly projectFilter = signal<number[]>([]);
  protected readonly sortField = signal<SortField>('ctime');
  protected readonly sortDirection = signal<SortDirection>('desc');
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly pageSizeOptions = [10, 50, 100, 300];
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

  protected readonly invalidPeriod = computed(() =>
    Boolean(this.periodStart() && this.periodEnd() && this.periodStart() > this.periodEnd()),
  );

  protected readonly visibleRequests = computed(() => {
    if (this.invalidPeriod()) return [];
    const direction = this.sortDirection() === 'asc' ? 1 : -1;
    return filterManagerVacationRequests(this.store.requests(), {
      email: this.emailFilter(),
      firstName: this.firstNameFilter(),
      lastName: this.lastNameFilter(),
      states: this.stateFilter(),
      periodStart: this.periodStart(),
      periodEnd: this.periodEnd(),
      projectIds: this.projectFilter(),
    }).sort((left, right) => this.sortValue(left).localeCompare(this.sortValue(right)) * direction);
  });

  protected readonly effectivePageIndex = computed(() => {
    const lastPage = Math.max(0, Math.ceil(this.visibleRequests().length / this.pageSize()) - 1);
    return Math.min(this.pageIndex(), lastPage);
  });

  protected readonly pagedRequests = computed(() => {
    const start = this.effectivePageIndex() * this.pageSize();
    return this.visibleRequests().slice(start, start + this.pageSize());
  });

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
      this.sortField.set('ctime');
      this.sortDirection.set('desc');
    } else {
      this.sortField.set(sort.active as SortField);
      this.sortDirection.set(sort.direction);
    }
    this.pageIndex.set(0);
  }

  protected changePage(page: PageEvent): void {
    this.pageIndex.set(page.pageIndex);
    this.pageSize.set(page.pageSize);
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

  private sortValue(item: ManagerVacationRequest): string {
    const request = item.request;
    return {
      email: request.author.email,
      firstName: request.author.firstName ?? '',
      lastName: request.author.lastName ?? '',
      ctime: request.ctime ?? '',
      startDate: request.startDate,
      endDate: request.endDate,
      requestState: request.requestState,
    }[this.sortField()].toLocaleLowerCase();
  }
}
