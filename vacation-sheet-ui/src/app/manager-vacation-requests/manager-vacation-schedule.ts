import { VacationRequestUser } from '../vacation-requests/vacation-requests.store';
import { ManagerVacationRequest } from './manager-vacation-requests.store';

const dayMilliseconds = 86_400_000;
const weekdayFormatter = new Intl.DateTimeFormat('ru-RU', { weekday: 'short', timeZone: 'UTC' });
const monthFormatter = new Intl.DateTimeFormat('ru-RU', {
  month: 'long',
  year: 'numeric',
  timeZone: 'UTC',
});

export interface CalendarDay {
  date: string;
  dayNumber: number;
  weekday: string;
  monthKey: string;
  isWeekend: boolean;
  isToday: boolean;
}

export interface CalendarMonth {
  key: string;
  label: string;
  startColumn: number;
  span: number;
}

export interface ScheduleBar {
  item: ManagerVacationRequest;
  track: number;
  startColumn: number;
  span: number;
}

export interface ScheduleEmployeeRow {
  author: VacationRequestUser;
  bars: ScheduleBar[];
  trackCount: number;
}

export function todayIso(): string {
  const today = new Date();
  return formatIsoDate(new Date(Date.UTC(today.getFullYear(), today.getMonth(), today.getDate())));
}

export function buildCalendar(today: string, daysAround: number): CalendarDay[] {
  const todayTime = parseIsoDate(today).getTime();
  return Array.from({ length: daysAround * 2 + 1 }, (_, index) => {
    const date = new Date(todayTime + (index - daysAround) * dayMilliseconds);
    const dayOfWeek = date.getUTCDay();
    const isoDate = formatIsoDate(date);
    return {
      date: isoDate,
      dayNumber: date.getUTCDate(),
      weekday: weekdayFormatter.format(date),
      monthKey: isoDate.slice(0, 7),
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6,
      isToday: isoDate === today,
    };
  });
}

export function buildCalendarMonths(days: CalendarDay[]): CalendarMonth[] {
  const months: CalendarMonth[] = [];
  days.forEach((day, index) => {
    const current = months.at(-1);
    if (current?.key === day.monthKey) {
      current.span += 1;
      return;
    }
    const date = parseIsoDate(day.date);
    months.push({
      key: day.monthKey,
      label: monthFormatter.format(date),
      startColumn: index + 1,
      span: 1,
    });
  });
  return months;
}

export function buildScheduleRows(
  items: ManagerVacationRequest[],
  rangeStart: string,
  rangeEnd: string,
): ScheduleEmployeeRow[] {
  const grouped = new Map<
    number,
    { author: VacationRequestUser; items: ManagerVacationRequest[] }
  >();
  items.forEach((item) => {
    const author = item.request.author;
    const group = grouped.get(author.id);
    if (group) group.items.push(item);
    else grouped.set(author.id, { author, items: [item] });
  });

  return [...grouped.values()].map(({ author, items }) => {
    const trackEnds: string[] = [];
    const bars = items
      .filter((item) => item.request.endDate >= rangeStart && item.request.startDate <= rangeEnd)
      .sort(
        (left, right) =>
          left.request.startDate.localeCompare(right.request.startDate) ||
          left.request.endDate.localeCompare(right.request.endDate),
      )
      .map((item) => {
        const start = item.request.startDate < rangeStart ? rangeStart : item.request.startDate;
        const end = item.request.endDate > rangeEnd ? rangeEnd : item.request.endDate;
        let track = trackEnds.findIndex((trackEnd) => item.request.startDate > trackEnd);
        if (track < 0) track = trackEnds.length;
        trackEnds[track] = item.request.endDate;
        return {
          item,
          track,
          startColumn: daysBetween(rangeStart, start) + 1,
          span: daysBetween(start, end) + 1,
        };
      });
    return { author, bars, trackCount: Math.max(1, trackEnds.length) };
  });
}

function parseIsoDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(Date.UTC(year, month - 1, day));
}

function formatIsoDate(date: Date): string {
  return [
    String(date.getUTCFullYear()).padStart(4, '0'),
    String(date.getUTCMonth() + 1).padStart(2, '0'),
    String(date.getUTCDate()).padStart(2, '0'),
  ].join('-');
}

function daysBetween(start: string, end: string): number {
  return Math.round(
    (parseIsoDate(end).getTime() - parseIsoDate(start).getTime()) / dayMilliseconds,
  );
}
