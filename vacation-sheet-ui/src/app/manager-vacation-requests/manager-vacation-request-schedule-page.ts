import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';

import { VacationRequestState, VacationType } from '../vacation-requests/vacation-requests.store';
import { ManagerVacationRequestReviewDialog } from './manager-vacation-request-review-dialog';
import {
  buildCalendar,
  buildCalendarMonths,
  buildScheduleRows,
  ScheduleBar,
  todayIso,
} from './manager-vacation-schedule';
import {
  ManagerVacationRequest,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';
import { ManagerVacationRequestsViewStore } from './manager-vacation-requests-view.store';

@Component({
  selector: 'app-manager-vacation-request-schedule-page',
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    RouterLink,
  ],
  templateUrl: './manager-vacation-request-schedule-page.html',
  styleUrl: './manager-vacation-request-schedule-page.scss',
})
export class ManagerVacationRequestSchedulePage implements OnInit, AfterViewInit {
  private readonly dialog = inject(MatDialog);
  private readonly timelineScroll = viewChild<ElementRef<HTMLDivElement>>('timelineScroll');
  protected readonly store = inject(ManagerVacationRequestsStore);
  protected readonly view = inject(ManagerVacationRequestsViewStore);
  protected readonly daysAround = signal(183);
  protected readonly today = todayIso();
  protected readonly invalidDays = computed(
    () => !Number.isInteger(this.daysAround()) || this.daysAround() < 1 || this.daysAround() > 366,
  );
  protected readonly effectiveDays = computed(() =>
    Math.min(366, Math.max(1, Math.trunc(this.daysAround()) || 1)),
  );
  protected readonly calendar = computed(() => buildCalendar(this.today, this.effectiveDays()));
  protected readonly scheduleWidth = computed(() => 256 + this.calendar().length * 32);
  protected readonly months = computed(() => buildCalendarMonths(this.calendar()));
  protected readonly rows = computed(() => {
    const calendar = this.calendar();
    return buildScheduleRows(
      this.view.visibleRequests(),
      calendar[0].date,
      calendar[calendar.length - 1].date,
    );
  });

  ngOnInit(): void {
    if (!this.store.requests().length) this.store.load();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.scrollToToday());
  }

  protected changeDays(value: string): void {
    this.daysAround.set(Number(value));
    setTimeout(() => this.scrollToToday());
  }

  protected openReview(item: ManagerVacationRequest, event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dialog.open(ManagerVacationRequestReviewDialog, {
      data: item,
      width: 'min(42rem, calc(100vw - 2rem))',
      maxWidth: '100vw',
    });
  }

  protected barTooltip(bar: ScheduleBar): string {
    const request = bar.item.request;
    const author =
      [request.author.firstName, request.author.lastName].filter(Boolean).join(' ') ||
      request.author.email;
    return `${request.title}\n${author}\n${request.startDate} – ${request.endDate}\n${this.typeLabel(request.vacationType)}\n${this.stateLabel(request.requestState)}`;
  }

  protected stateLabel(state: VacationRequestState): string {
    return { DRAFT: 'Черновик', READY: 'Готово', APPROVED: 'Одобрено', REJECTED: 'Отклонено' }[
      state
    ];
  }

  protected typeLabel(type: VacationType): string {
    return type === 'PAYMENT_VACATION' ? 'Оплачиваемый отпуск' : 'За свой счёт';
  }

  private scrollToToday(): void {
    const element = this.timelineScroll()?.nativeElement;
    if (!element) return;
    const employeeColumnWidth = 256;
    const dayWidth = 32;
    element.scrollLeft =
      employeeColumnWidth +
      this.effectiveDays() * dayWidth +
      dayWidth / 2 -
      element.clientWidth / 2;
  }
}
