import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { VacationType } from '../vacation-requests/vacation-requests.store';
import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';

@Component({
  selector: 'app-manager-vacation-request-review-page',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './manager-vacation-request-review-page.html',
  styleUrl: './manager-vacation-request-review-page.scss',
})
export class ManagerVacationRequestReviewPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly store = inject(ManagerVacationRequestsStore);
  protected readonly item = signal<ManagerVacationRequest | null>(null);
  protected readonly loading = signal(false);

  protected readonly form = this.formBuilder.nonNullable.group({
    requestState: this.formBuilder.nonNullable.control<ManagerVacationRequestState>('READY'),
    managerComment: ['', Validators.maxLength(2000)],
  });

  ngOnInit(): void {
    this.store.error.set(null);
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isSafeInteger(id) || id <= 0) {
      this.store.error.set('Некорректный идентификатор заявления');
      return;
    }

    this.loading.set(true);
    this.store.loadOne(id).subscribe({
      next: (item) => {
        this.item.set(item);
        this.form.setValue({
          requestState: item.request.requestState as ManagerVacationRequestState,
          managerComment: item.request.managerComments ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.store.error.set('Не удалось загрузить заявление на рассмотрение');
        this.loading.set(false);
      },
    });
  }

  protected submit(): void {
    const item = this.item();
    if (!item || this.form.invalid || this.store.saving()) return;
    const value = this.form.getRawValue();
    this.store.review(
      item.request.id,
      {
        managerComment: value.managerComment || null,
        updateManagerComment: true,
        requestState: value.requestState,
      },
      () => this.router.navigate(['/profile/vacation-requests/review']),
    );
  }

  protected typeLabel(type: VacationType): string {
    return type === 'PAYMENT_VACATION' ? 'Оплачиваемый отпуск' : 'Отпуск за свой счёт';
  }

  protected dateLabel(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}.${month}.${year}`;
  }
}
