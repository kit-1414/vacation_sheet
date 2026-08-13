import { DatePipe, SlicePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { ProjectsStore } from './projects.store';
import { AuthStore } from '../auth.store';

@Component({
  selector: 'app-projects-page',
  imports: [
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTableModule,
    DatePipe,
    SlicePipe,
    RouterLink,
  ],
  templateUrl: './projects-page.html',
  styleUrl: './projects-page.scss',
})
export class ProjectsPage implements OnInit {
  protected readonly store = inject(ProjectsStore);
  protected readonly auth = inject(AuthStore);
  protected readonly selectedIds = signal(new Set<number>());
  protected readonly columns = [
    'select',
    'name',
    'ctime',
    'teamSize',
    'managerCount',
    'description',
    'edit',
    'relations',
  ];

  ngOnInit(): void {
    this.store.load();
  }

  protected isSelected(id: number): boolean {
    return this.selectedIds().has(id);
  }

  protected toggle(id: number): void {
    if (!this.auth.canAdminister()) return;
    this.selectedIds.update((selectedIds) => {
      const updated = new Set(selectedIds);
      updated.has(id) ? updated.delete(id) : updated.add(id);
      return updated;
    });
  }

  protected allSelected(): boolean {
    return this.store.projects().length > 0 && this.selectedIds().size === this.store.projects().length;
  }

  protected toggleAll(): void {
    if (!this.auth.canAdminister()) return;
    this.selectedIds.set(this.allSelected() ? new Set() : new Set(this.store.projects().map((project) => project.id)));
  }

  protected deleteSelected(): void {
    if (!this.auth.canAdminister()) return;
    this.selectedIds().forEach((id) => this.store.delete(id));
    this.selectedIds.set(new Set());
  }
}
