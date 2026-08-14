import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { UserAccount, UsersStore } from '../users/users.store';
import { AuthStore } from '../auth.store';
import { Project, ProjectsStore } from './projects.store';

type UserSortColumn = 'email' | 'firstName' | 'lastName' | 'isAdmin' | 'isActive';

@Component({
  selector: 'app-project-members-page',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './project-members-page.html',
  styleUrl: './project-members-page.scss',
})
export class ProjectMembersPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  protected readonly projectsStore = inject(ProjectsStore);
  protected readonly usersStore = inject(UsersStore);
  private readonly auth = inject(AuthStore);
  protected readonly project = signal<Project | null>(null);
  protected readonly loading = signal(true);
  protected readonly columns = ['email', 'firstName', 'lastName', 'isAdmin', 'isActive', 'member', 'manager'];
  protected readonly filter = signal('');
  protected readonly sortColumn = signal<UserSortColumn>('email');
  protected readonly sortDirection = signal<'asc' | 'desc'>('asc');
  protected readonly visibleUsers = computed(() => {
    const pattern = this.filter().trim().toLocaleLowerCase();
    const column = this.sortColumn();
    const direction = this.sortDirection() === 'asc' ? 1 : -1;

    const users = this.usersStore.users()
      .filter((user) => [user.email, user.firstName, user.lastName, this.booleanLabel(user.isAdmin), this.booleanLabel(user.isActive)]
        .some((value) => value?.toLocaleLowerCase().includes(pattern)));
    return users.sort((left, right) => this.sortValue(left, column).localeCompare(this.sortValue(right, column)) * direction);
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.loading.set(false);
      return;
    }
    const projectId = Number(id);
    if (!Number.isSafeInteger(projectId)) {
      this.loading.set(false);
      return;
    }

    this.usersStore.load();
    this.projectsStore.loadOne(projectId).subscribe({
      next: (project) => {
        this.project.set(project);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected isMember(userId: number): boolean {
    return this.project()?.members.some((user) => user.id === userId) ?? false;
  }

  protected isManager(userId: number): boolean {
    return this.project()?.managers.some((user) => user.id === userId) ?? false;
  }

  protected setFilter(value: string): void {
    this.filter.set(value);
  }

  protected sortBy(column: UserSortColumn): void {
    if (this.sortColumn() === column) {
      this.sortDirection.update((direction) => direction === 'asc' ? 'desc' : 'asc');
      return;
    }
    this.sortColumn.set(column);
    this.sortDirection.set('asc');
  }

  protected sortIndicator(column: UserSortColumn): string {
    if (this.sortColumn() !== column) return '';
    return this.sortDirection() === 'asc' ? ' ↑' : ' ↓';
  }

  private sortValue(user: UserAccount, column: UserSortColumn): string {
    const value = user[column];
    return typeof value === 'boolean' ? this.booleanLabel(value) : value ?? '';
  }

  private booleanLabel(value: boolean): string {
    return value ? 'Да' : 'Нет';
  }

  protected updateMembership(user: UserAccount): void {
    if (!this.auth.canManageRelations()) return;
    const projectId = this.project()?.id;
    if (!projectId) return;

    const onSuccess = (project: Project) => this.project.set(project);
    if (this.isMember(user.id)) {
      this.projectsStore.removeMember(projectId, user.id, onSuccess);
    } else {
      this.projectsStore.addMember(projectId, user.id, onSuccess);
    }
  }

  protected updateManagement(user: UserAccount): void {
    if (!this.auth.canManageRelations()) return;
    const projectId = this.project()?.id;
    if (!projectId) return;

    const onSuccess = (project: Project) => this.project.set(project);
    if (this.isManager(user.id)) {
      this.projectsStore.removeManager(projectId, user.id, onSuccess);
    } else {
      this.projectsStore.addManager(projectId, user.id, onSuccess);
    }
  }
}
