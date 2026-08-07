import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { UserAccount, UsersStore } from './users.store';

@Component({
  selector: 'app-user-editor-page',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './user-editor-page.html',
  styleUrl: './user-editor-page.scss',
})
export class UserEditorPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly store = inject(UsersStore);
  protected readonly user = signal<UserAccount | null>(null);
  protected readonly loading = signal(false);

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(320)]],
    firstName: ['', Validators.maxLength(255)],
    lastName: ['', Validators.maxLength(255)],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    const userId = Number(id);
    if (!Number.isSafeInteger(userId)) return;

    this.loading.set(true);
    this.store.loadOne(userId).subscribe({
      next: (user) => {
        this.user.set(user);
        this.form.setValue({
          email: user.email,
          firstName: user.firstName ?? '',
          lastName: user.lastName ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.store.error.set('Не удалось загрузить пользователя');
        this.loading.set(false);
      },
    });
  }

  protected submit(): void {
    if (this.form.invalid || this.store.saving()) return;
    const value = this.form.getRawValue();
    const request = {
      email: value.email,
      firstName: value.firstName || null,
      lastName: value.lastName || null,
    };
    const onSuccess = () => this.router.navigate(['/users']);
    const id = this.user()?.id;
    if (id) {
      this.store.update(id, request, onSuccess);
    } else {
      this.store.create(request, onSuccess);
    }
  }
}
