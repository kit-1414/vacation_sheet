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

import { AuthStore } from '../auth.store';
import { dateRangeValidator, strictDateValidator } from './vacation-request-date.validators';
import {
  VacationRequest,
  VacationRequestPayload,
  VacationRequestState,
  VacationRequestsStore,
} from './vacation-requests.store';

@Component({
  selector: 'app-vacation-request-editor-page',
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
  protected readonly readOnly = signal(false);
  protected readonly viewOnly = this.route.snapshot.data['viewOnly'] === true;

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      title: ['', [Validators.required, Validators.maxLength(50)]],
      requestState: this.formBuilder.nonNullable.control<VacationRequestState>('DRAFT', Validators.required),
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
      return;
    }

    this.loading.set(true);
    this.store.loadOne(requestId).subscribe({
      next: (request) => {
        this.request.set(request);
        this.form.setValue({
          title: request.title,
          requestState: request.requestState,
          vacationType: request.vacationType,
          startDate: request.startDate,
          endDate: request.endDate,
          userComments: request.userComments ?? '',
        });
        if (
          this.viewOnly ||
          request.author.id !== this.auth.user()?.id ||
          !this.isEditableState(request.requestState)
        ) {
          this.readOnly.set(true);
          this.form.disable();
        }
        this.loading.set(false);
      },
      error: () => {
        this.store.error.set('Не удалось загрузить заявление');
        this.loading.set(false);
      },
    });
  }

  protected submit(): void {
    if (
      this.readOnly() ||
      !this.auth.canManageVacationRequests() ||
      this.form.invalid ||
      this.store.saving()
    )
      return;
    const value = this.form.getRawValue();
    if (!this.isEditableState(value.requestState)) return;
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

  protected stateLabel(state: VacationRequestState): string {
    return {
      DRAFT: 'Черновик',
      READY: 'Готово к согласованию',
      APPROVED: 'Одобрено',
      REJECTED: 'Отклонено',
    }[state];
  }

  private isEditableState(
    state: VacationRequestState,
  ): state is VacationRequestPayload['requestState'] {
    return state === 'DRAFT' || state === 'READY';
  }
}
