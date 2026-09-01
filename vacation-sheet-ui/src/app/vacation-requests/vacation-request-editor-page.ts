import { Component, OnInit, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthStore } from '../auth.store';
import {
  VacationRequest,
  VacationRequestPayload,
  VacationRequestsStore,
} from './vacation-requests.store';

export const strictDateValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(control.value as string);
  if (!match) return { invalidDate: true };

  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  if (year === 0 || month < 1 || month > 12) return { invalidDate: true };

  const leapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
  const daysInMonth = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  return day >= 1 && day <= daysInMonth[month - 1] ? null : { invalidDate: true };
};

export const dateRangeValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const startDate = control.get('startDate')?.value as string | undefined;
  const endDate = control.get('endDate')?.value as string | undefined;
  return startDate && endDate && endDate < startDate ? { dateRange: true } : null;
};

@Component({
  selector: 'app-vacation-request-editor-page',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './vacation-request-editor-page.html',
  styleUrl: './vacation-request-editor-page.scss',
})
export class VacationRequestEditorPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthStore);
  protected readonly store = inject(VacationRequestsStore);
  protected readonly request = signal<VacationRequest | null>(null);
  protected readonly loading = signal(false);
  protected readonly editable = signal(true);

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      title: ['', [Validators.required, Validators.maxLength(50)]],
      requestState: this.formBuilder.nonNullable.control<VacationRequestPayload['requestState']>(
        'DRAFT',
        Validators.required,
      ),
      vacationType: this.formBuilder.nonNullable.control<VacationRequestPayload['vacationType']>(
        'PAYMENT_VACATION',
        Validators.required,
      ),
      startDate: ['', [Validators.required, strictDateValidator]],
      endDate: ['', [Validators.required, strictDateValidator]],
      userComments: ['', Validators.maxLength(2000)],
    },
    { validators: dateRangeValidator },
  );

  ngOnInit(): void {
    this.store.error.set(null);
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    const requestId = Number(id);
    if (!Number.isSafeInteger(requestId) || requestId <= 0) {
      this.store.error.set('Некорректный идентификатор заявления');
      this.editable.set(false);
      return;
    }

    this.loading.set(true);
    this.store.loadOne(requestId).subscribe({
      next: (request) => {
        if (request.requestState !== 'DRAFT' && request.requestState !== 'READY') {
          this.store.error.set(
            'Редактировать можно только черновик или готовое к согласованию заявление',
          );
          this.editable.set(false);
          this.loading.set(false);
          return;
        }
        this.request.set(request);
        this.form.setValue({
          title: request.title,
          requestState: request.requestState,
          vacationType: request.vacationType,
          startDate: request.startDate,
          endDate: request.endDate,
          userComments: request.userComments ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.store.error.set('Не удалось загрузить заявление');
        this.editable.set(false);
        this.loading.set(false);
      },
    });
  }

  protected submit(): void {
    if (!this.auth.canManageVacationRequests() || this.form.invalid || this.store.saving()) return;
    const value = this.form.getRawValue();
    const payload: VacationRequestPayload = {
      title: value.title,
      requestState: value.requestState,
      vacationType: value.vacationType,
      startDate: value.startDate,
      endDate: value.endDate,
      userComments: value.userComments || null,
    };
    const onSuccess = () => this.router.navigate(['/profile/vacation-requests']);
    const id = this.request()?.id;
    if (id !== undefined) {
      this.store.update(id, payload, onSuccess);
    } else {
      this.store.create(payload, onSuccess);
    }
  }
}
