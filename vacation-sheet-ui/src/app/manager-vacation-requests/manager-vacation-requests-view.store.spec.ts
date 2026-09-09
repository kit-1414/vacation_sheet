import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ManagerVacationRequestsStore } from './manager-vacation-requests.store';
import { ManagerVacationRequestsViewStore } from './manager-vacation-requests-view.store';

describe('ManagerVacationRequestsViewStore', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('keeps filters, sorting and pagination in the root service instance', () => {
    TestBed.configureTestingModule({
      providers: [
        ManagerVacationRequestsViewStore,
        { provide: ManagerVacationRequestsStore, useValue: { requests: signal([]) } },
      ],
    });
    const view = TestBed.inject(ManagerVacationRequestsViewStore);

    view.emailFilter.set('user@example.com');
    view.stateFilter.set(['REJECTED']);
    view.setSort('lastName', 'asc');
    view.setPage(2, 50);

    const restored = TestBed.inject(ManagerVacationRequestsViewStore);
    expect(restored.emailFilter()).toBe('user@example.com');
    expect(restored.stateFilter()).toEqual(['REJECTED']);
    expect(restored.sortField()).toBe('lastName');
    expect(restored.sortDirection()).toBe('asc');
    expect(restored.pageIndex()).toBe(2);
    expect(restored.pageSize()).toBe(50);
  });

  it('rejects invalid calendar dates in the period filter', () => {
    TestBed.configureTestingModule({
      providers: [
        ManagerVacationRequestsViewStore,
        { provide: ManagerVacationRequestsStore, useValue: { requests: signal([]) } },
      ],
    });
    const view = TestBed.inject(ManagerVacationRequestsViewStore);

    view.periodStart.set('2026-02-30');

    expect(view.invalidDates()).toBe(true);
    expect(view.visibleRequests()).toEqual([]);
  });

  it('shows current approved vacations without changing sorting or pagination', () => {
    TestBed.configureTestingModule({
      providers: [
        ManagerVacationRequestsViewStore,
        { provide: ManagerVacationRequestsStore, useValue: { requests: signal([]) } },
      ],
    });
    const view = TestBed.inject(ManagerVacationRequestsViewStore);
    view.emailFilter.set('user@example.com');
    view.firstNameFilter.set('Test');
    view.lastNameFilter.set('User');
    view.stateFilter.set(['READY', 'REJECTED']);
    view.periodStart.set('2026-01-01');
    view.periodEnd.set('2026-12-31');
    view.projectFilter.set([1, 2]);
    view.setSort('lastName', 'asc');
    view.setPage(3, 50);

    view.showCurrentVacations('2026-09-09');

    expect(view.emailFilter()).toBe('');
    expect(view.firstNameFilter()).toBe('');
    expect(view.lastNameFilter()).toBe('');
    expect(view.stateFilter()).toEqual(['APPROVED']);
    expect(view.periodStart()).toBe('2026-09-09');
    expect(view.periodEnd()).toBe('2026-09-09');
    expect(view.projectFilter()).toEqual([]);
    expect(view.sortField()).toBe('lastName');
    expect(view.sortDirection()).toBe('asc');
    expect(view.pageIndex()).toBe(3);
    expect(view.pageSize()).toBe(50);
  });

  it('resets all filters without changing sorting or pagination', () => {
    TestBed.configureTestingModule({
      providers: [
        ManagerVacationRequestsViewStore,
        { provide: ManagerVacationRequestsStore, useValue: { requests: signal([]) } },
      ],
    });
    const view = TestBed.inject(ManagerVacationRequestsViewStore);
    view.emailFilter.set('user@example.com');
    view.firstNameFilter.set('Test');
    view.lastNameFilter.set('User');
    view.stateFilter.set(['APPROVED']);
    view.periodStart.set('2026-09-01');
    view.periodEnd.set('2026-09-30');
    view.projectFilter.set([1]);
    view.setSort('email', 'asc');
    view.setPage(2, 100);

    view.resetFilters();

    expect(view.emailFilter()).toBe('');
    expect(view.firstNameFilter()).toBe('');
    expect(view.lastNameFilter()).toBe('');
    expect(view.stateFilter()).toEqual([]);
    expect(view.periodStart()).toBe('');
    expect(view.periodEnd()).toBe('');
    expect(view.projectFilter()).toEqual([]);
    expect(view.sortField()).toBe('email');
    expect(view.sortDirection()).toBe('asc');
    expect(view.pageIndex()).toBe(2);
    expect(view.pageSize()).toBe(100);
  });
});
