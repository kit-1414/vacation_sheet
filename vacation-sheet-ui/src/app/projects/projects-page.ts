import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { Project, ProjectsStore } from './projects.store';

@Component({
  selector: 'app-projects-page',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  templateUrl: './projects-page.html',
  styleUrl: './projects-page.scss',
})
export class ProjectsPage implements OnInit {
  protected readonly store = inject(ProjectsStore);
  protected readonly editingId = signal<string | null>(null);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(2000)],
  });

  ngOnInit(): void {
    this.store.load();
  }

  protected submit(): void {
    if (this.form.invalid) return;
    const request = {
      name: this.form.controls.name.value,
      description: this.form.controls.description.value || null,
    };
    const editingId = this.editingId();
    if (editingId) {
      this.store.update(editingId, request);
    } else {
      this.store.create(request);
    }
    this.cancelEdit();
  }

  protected edit(project: Project): void {
    this.editingId.set(project.id);
    this.form.setValue({ name: project.name, description: project.description ?? '' });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({ name: '', description: '' });
  }

  protected availableUsers(project: Project) {
    const memberIds = new Set(project.members.map((member) => member.id));
    return this.store.users().filter((user) => !memberIds.has(user.id));
  }
}
