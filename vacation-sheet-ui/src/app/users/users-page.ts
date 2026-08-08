import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { UsersStore } from './users.store';

@Component({
  selector: 'app-users-page',
  imports: [DatePipe, MatButtonModule, MatCardModule, MatCheckboxModule, MatProgressSpinnerModule, MatTableModule, RouterLink],
  templateUrl: './users-page.html',
  styleUrl: './users-page.scss',
})
export class UsersPage implements OnInit {
  protected readonly store = inject(UsersStore);
  protected readonly selectedIds = signal(new Set<number>());
  protected readonly columns = ['select', 'lastName', 'firstName', 'email', 'ctime', 'utime', 'edit'];

  ngOnInit(): void {
    this.store.load();
  }

  protected isSelected(id: number): boolean {
    return this.selectedIds().has(id);
  }

  protected toggle(id: number): void {
    this.selectedIds.update((selectedIds) => {
      const updated = new Set(selectedIds);
      updated.has(id) ? updated.delete(id) : updated.add(id);
      return updated;
    });
  }

  protected allSelected(): boolean {
    return this.store.users().length > 0 && this.selectedIds().size === this.store.users().length;
  }

  protected toggleAll(): void {
    this.selectedIds.set(this.allSelected() ? new Set() : new Set(this.store.users().map((user) => user.id)));
  }

  protected deleteSelected(): void {
    this.selectedIds().forEach((id) => this.store.delete(id));
    this.selectedIds.set(new Set());
  }
}
