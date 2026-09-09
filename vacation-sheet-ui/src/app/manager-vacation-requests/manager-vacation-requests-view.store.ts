import { Injectable, computed, inject, signal } from '@angular/core';

import { isValidIsoDate } from '../vacation-requests/vacation-request-date.validators';
import { filterManagerVacationRequests } from './manager-vacation-request-filter';
import { todayIso } from './manager-vacation-schedule';
import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';

export type ManagerSortField =
  'email' | 'firstName' | 'lastName' | 'ctime' | 'startDate' | 'endDate' | 'requestState';
export type ManagerSortDirection = 'asc' | 'desc';

@Injectable({ providedIn: 'root' })
export class ManagerVacationRequestsViewStore {
  private readonly requestsStore = inject(ManagerVacationRequestsStore);

  readonly emailFilter = signal('');
  readonly firstNameFilter = signal('');
  readonly lastNameFilter = signal('');
  readonly stateFilter = signal<ManagerVacationRequestState[]>(['READY', 'APPROVED']);
  readonly periodStart = signal('');
  readonly periodEnd = signal('');
  readonly projectFilter = signal<number[]>([]);
  readonly sortField = signal<ManagerSortField>('ctime');
  readonly sortDirection = signal<ManagerSortDirection>('desc');
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly pageSizeOptions = [10, 50, 100, 300];

  readonly invalidDates = computed(
    () =>
      (Boolean(this.periodStart()) && !isValidIsoDate(this.periodStart())) ||
      (Boolean(this.periodEnd()) && !isValidIsoDate(this.periodEnd())),
  );
  readonly invalidPeriod = computed(
    () =>
      !this.invalidDates() &&
      Boolean(this.periodStart() && this.periodEnd() && this.periodStart() > this.periodEnd()),
  );

  readonly visibleRequests = computed(() => {
    if (this.invalidDates() || this.invalidPeriod()) return [];
    const direction = this.sortDirection() === 'asc' ? 1 : -1;
    return filterManagerVacationRequests(this.requestsStore.requests(), {
      email: this.emailFilter(),
      firstName: this.firstNameFilter(),
      lastName: this.lastNameFilter(),
      states: this.stateFilter(),
      periodStart: this.periodStart(),
      periodEnd: this.periodEnd(),
      projectIds: this.projectFilter(),
    }).sort((left, right) => this.sortValue(left).localeCompare(this.sortValue(right)) * direction);
  });

  readonly effectivePageIndex = computed(() => {
    const lastPage = Math.max(0, Math.ceil(this.visibleRequests().length / this.pageSize()) - 1);
    return Math.min(this.pageIndex(), lastPage);
  });

  readonly pagedRequests = computed(() => {
    const start = this.effectivePageIndex() * this.pageSize();
    return this.visibleRequests().slice(start, start + this.pageSize());
  });

  setSort(field: ManagerSortField, direction: ManagerSortDirection): void {
    this.sortField.set(field);
    this.sortDirection.set(direction);
    this.pageIndex.set(0);
  }

  setPage(pageIndex: number, pageSize: number): void {
    this.pageIndex.set(pageIndex);
    this.pageSize.set(pageSize);
  }

  resetFilters(): void {
    this.emailFilter.set('');
    this.firstNameFilter.set('');
    this.lastNameFilter.set('');
    this.stateFilter.set([]);
    this.periodStart.set('');
    this.periodEnd.set('');
    this.projectFilter.set([]);
  }

  showCurrentVacations(today = todayIso()): void {
    this.resetFilters();
    this.stateFilter.set(['APPROVED']);
    this.periodStart.set(today);
    this.periodEnd.set(today);
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
