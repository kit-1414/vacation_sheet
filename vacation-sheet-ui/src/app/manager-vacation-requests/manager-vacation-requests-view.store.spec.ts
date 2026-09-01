import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ManagerVacationRequestsStore } from './manager-vacation-requests.store';
import { ManagerVacationRequestsViewStore } from './manager-vacation-requests-view.store';

describe('ManagerVacationRequestsViewStore', () => {
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
});
