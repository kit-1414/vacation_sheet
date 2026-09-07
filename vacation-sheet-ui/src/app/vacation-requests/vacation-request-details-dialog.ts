import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { ManagerVacationRequest } from '../manager-vacation-requests/manager-vacation-requests.store';
import { dateRangeValidator, strictDateValidator } from './vacation-request-date.validators';
import {
  VacationRequestPayload,
  VacationRequestState,
  VacationRequestsStore,
} from './vacation-requests.store';

export interface VacationRequestDetailsDialogData {
  item: ManagerVacationRequest;
  editable: boolean;
}

@Component({
  selector: 'app-vacation-request-details-dialog',
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  templateUrl: './vacation-request-details-dialog.html',
  styleUrl: './vacation-request-details-dialog.scss',
})
export class VacationRequestDetailsDialog {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<VacationRequestDetailsDialog>);
  protected readonly requestsStore = inject(VacationRequestsStore);
  protected readonly data = inject<VacationRequestDetailsDialogData>(MAT_DIALOG_DATA);
  protected readonly request = this.data.item.request;

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      title: [this.request.title, [Validators.required, Validators.maxLength(50)]],
      requestState: this.formBuilder.nonNullable.control<VacationRequestState>(
        this.request.requestState,
        Validators.required,
      ),
      vacationType: this.formBuilder.nonNullable.control(this.request.vacationType, Validators.required),
      startDate: [this.request.startDate, [Validators.required, strictDateValidator]],
      endDate: [this.request.endDate, [Validators.required, strictDateValidator]],
      userComments: [this.request.userComments ?? '', Validators.maxLength(2000)],
    },
    { validators: dateRangeValidator },
  );

  constructor() {
    this.requestsStore.error.set(null);
    if (!this.data.editable) this.form.disable();
  }

  protected submit(): void {
    if (!this.data.editable || this.form.invalid || this.requestsStore.saving()) return;
    const value = this.form.getRawValue();
    if (value.requestState !== 'DRAFT' && value.requestState !== 'READY') return;
    const payload: VacationRequestPayload = {
      title: value.title,
      requestState: value.requestState,
      vacationType: value.vacationType,
      startDate: value.startDate,
      endDate: value.endDate,
      userComments: value.userComments || null,
    };
    this.requestsStore.update(this.request.id, payload, (request) => this.dialogRef.close(request));
  }

  protected close(): void {
    this.dialogRef.close();
  }

  protected projectsLabel(): string {
    return this.data.item.authorProjects.map((project) => project.name).join(', ') || 'Нет проектов';
  }
}
