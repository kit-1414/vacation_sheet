import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Project, ProjectsStore } from './projects.store';
import { AuthStore } from '../auth.store';

@Component({
  selector: 'app-project-editor-page',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './project-editor-page.html',
  styleUrl: './project-editor-page.scss',
})
export class ProjectEditorPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly store = inject(ProjectsStore);
  private readonly auth = inject(AuthStore);
  protected readonly project = signal<Project | null>(null);
  protected readonly loading = signal(false);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/^[A-Za-z0-9]+$/)]],
    description: ['', Validators.maxLength(1000)],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    const projectId = Number(id);
    if (!Number.isSafeInteger(projectId)) return;

    this.loading.set(true);
    this.store.loadOne(projectId).subscribe({
      next: (project) => {
        this.project.set(project);
        this.form.setValue({ name: project.name, description: project.description ?? '' });
        this.loading.set(false);
      },
      error: () => {
        this.store.error.set('Не удалось загрузить проект');
        this.loading.set(false);
      },
    });
  }

  protected submit(): void {
    if (!this.auth.canAdminister() || this.form.invalid || this.store.saving()) return;
    const request = {
      name: this.form.controls.name.value,
      description: this.form.controls.description.value || null,
    };
    const id = this.project()?.id;
    if (id) {
      this.store.update(id, request);
    } else {
      this.store.create(request);
    }
    this.router.navigate(['/projects']);
  }
}
