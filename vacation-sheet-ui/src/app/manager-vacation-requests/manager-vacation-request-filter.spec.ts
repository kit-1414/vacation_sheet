import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
} from './manager-vacation-requests.store';
import { filterManagerVacationRequests } from './manager-vacation-request-filter';

describe('filterManagerVacationRequests', () => {
  const request = (
    id: number,
    state: ManagerVacationRequestState,
    startDate: string,
    endDate: string,
    projectIds: number[],
  ): ManagerVacationRequest => ({
    request: {
      id,
      title: `Vacation ${id}`,
      requestState: state,
      vacationType: 'PAYMENT_VACATION',
      startDate,
      endDate,
      userComments: null,
      author: { id, email: `User${id}@Example.com`, firstName: 'Иван', lastName: 'Иванов' },
      ctime: null,
      utime: null,
    },
    authorProjects: projectIds.map((projectId) => ({
      id: projectId,
      name: `Project ${projectId}`,
    })),
  });
  const requests = [
    request(1, 'READY', '2026-09-01', '2026-09-10', [1]),
    request(2, 'APPROVED', '2026-09-20', '2026-09-30', [2]),
    request(3, 'REJECTED', '2026-10-01', '2026-10-10', [3]),
    request(4, 'PREVIEW', '2026-08-01', '2026-08-10', [4]),
    request(5, 'IN_PROGRESS', '2026-08-11', '2026-08-20', [5]),
  ];

  it('uses the configured initial state filter', () => {
    const result = filterManagerVacationRequests(requests, {
      email: '',
      firstName: '',
      lastName: '',
      states: ['PREVIEW', 'READY', 'IN_PROGRESS', 'APPROVED'],
      periodStart: '',
      periodEnd: '',
      projectIds: [],
    });

    expect(result.map((item) => item.request.id)).toEqual([1, 2, 4, 5]);
  });

  it('matches vacations intersecting an open or closed interval', () => {
    const closed = filterManagerVacationRequests(requests, {
      email: '',
      firstName: '',
      lastName: '',
      states: [],
      periodStart: '2026-09-08',
      periodEnd: '2026-09-22',
      projectIds: [],
    });
    const open = filterManagerVacationRequests(requests, {
      email: '',
      firstName: '',
      lastName: '',
      states: [],
      periodStart: '2026-09-25',
      periodEnd: '',
      projectIds: [],
    });

    expect(closed.map((item) => item.request.id)).toEqual([1, 2]);
    expect(open.map((item) => item.request.id)).toEqual([2, 3]);
  });

  it('matches any selected project and text case-insensitively', () => {
    const result = filterManagerVacationRequests(requests, {
      email: 'user2@example',
      firstName: 'ив',
      lastName: 'ИВА',
      states: [],
      periodStart: '',
      periodEnd: '',
      projectIds: [1, 2],
    });

    expect(result.map((item) => item.request.id)).toEqual([2]);
  });
});
