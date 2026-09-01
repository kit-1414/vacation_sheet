import { buildCalendar, buildCalendarMonths, buildScheduleRows } from './manager-vacation-schedule';
import { ManagerVacationRequest } from './manager-vacation-requests.store';

describe('manager vacation schedule', () => {
  it('places today in the center of the calendar', () => {
    const calendar = buildCalendar('2026-09-15', 2);

    expect(calendar.map((day) => day.date)).toEqual([
      '2026-09-13',
      '2026-09-14',
      '2026-09-15',
      '2026-09-16',
      '2026-09-17',
    ]);
    expect(calendar[2].isToday).toBe(true);
  });

  it('groups calendar days by month', () => {
    const months = buildCalendarMonths(buildCalendar('2026-10-01', 2));

    expect(months.map(({ key, startColumn, span }) => ({ key, startColumn, span }))).toEqual([
      { key: '2026-09', startColumn: 1, span: 2 },
      { key: '2026-10', startColumn: 3, span: 3 },
    ]);
  });

  it('preserves employee order and puts overlapping requests on separate tracks', () => {
    const rows = buildScheduleRows(
      [
        item(1, 2, '2026-09-10', '2026-09-20'),
        item(2, 1, '2026-09-12', '2026-09-13'),
        item(3, 2, '2026-09-15', '2026-09-18'),
        item(4, 2, '2026-09-21', '2026-09-22'),
      ],
      '2026-09-12',
      '2026-09-22',
    );

    expect(rows.map((row) => row.author.id)).toEqual([2, 1]);
    expect(rows[0].trackCount).toBe(2);
    expect(rows[0].bars.map((bar) => bar.track)).toEqual([0, 1, 0]);
    expect(rows[0].bars[0]).toMatchObject({ startColumn: 1, span: 9 });
  });
});

function item(
  id: number,
  authorId: number,
  startDate: string,
  endDate: string,
): ManagerVacationRequest {
  return {
    request: {
      id,
      title: `Vacation ${id}`,
      requestState: 'READY',
      vacationType: 'PAYMENT_VACATION',
      startDate,
      endDate,
      userComments: null,
      managerComments: null,
      author: {
        id: authorId,
        email: `user${authorId}@example.com`,
        firstName: null,
        lastName: null,
      },
      manager: null,
      ctime: null,
      utime: null,
    },
    authorProjects: [],
  };
}
