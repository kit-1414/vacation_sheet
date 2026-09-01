import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
  ManagerVacationRequestsStore,
} from './manager-vacation-requests.store';

@Component({
  selector: 'app-manager-vacation-request-review-dialog',
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  templateUrl: './manager-vacation-request-review-dialog.html',
  styleUrl: './manager-vacation-request-review-dialog.scss',
})
export class ManagerVacationRequestReviewDialog {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ManagerVacationRequestReviewDialog>);
  protected readonly data = inject<ManagerVacationRequest>(MAT_DIALOG_DATA);
  protected readonly store = inject(ManagerVacationRequestsStore);
  protected readonly projectNames = this.data.authorProjects
    .map((project) => project.name)
    .join(', ');

  protected readonly form = this.formBuilder.nonNullable.group({
    requestState: this.formBuilder.nonNullable.control<ManagerVacationRequestState>(
      this.data.request.requestState as ManagerVacationRequestState,
    ),
    managerComment: [this.data.request.managerComments ?? '', Validators.maxLength(2000)],
  });

  protected submit(): void {
    if (this.form.invalid || this.store.saving()) return;
    const value = this.form.getRawValue();
    this.store.review(
      this.data.request.id,
      {
        managerComment: value.managerComment || null,
        updateManagerComment: true,
        requestState: value.requestState,
      },
      (updated) => this.dialogRef.close(updated),
    );
  }
}
