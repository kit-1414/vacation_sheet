import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

import { UserAccount } from '../users/users.store';

export interface Project {
  id: string;
  name: string;
  description: string | null;
  members: UserAccount[];
  ctime: string | null;
  utime: string | null;
}

export interface ProjectRequest {
  name: string;
  description: string | null;
}

@Injectable({ providedIn: 'root' })
export class ProjectsStore {
  private readonly http = inject(HttpClient);

  readonly projects = signal<Project[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<Project[]>('/api/projects').subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: () => this.fail('Не удалось загрузить проекты'),
    });
  }

  create(request: ProjectRequest): void {
    this.saving.set(true);
    this.http.post<Project>('/api/projects', request).subscribe({
      next: (project) => {
        this.projects.update((projects) => [...projects, project].sort(this.byName));
        this.saving.set(false);
      },
      error: () => this.fail('Не удалось создать проект'),
    });
  }

  update(id: string, request: ProjectRequest): void {
    this.saving.set(true);
    this.http.put<Project>(`/api/projects/${id}`, request).subscribe({
      next: (project) => this.replace(project),
      error: () => this.fail('Не удалось обновить проект'),
    });
  }

  loadOne(id: string): Observable<Project> {
    return this.http.get<Project>(`/api/projects/${id}`);
  }

  delete(id: string): void {
    this.http.delete<void>(`/api/projects/${id}`).subscribe({
      next: () => this.projects.update((projects) => projects.filter((project) => project.id !== id)),
      error: () => this.fail('Не удалось удалить проект'),
    });
  }

  addMember(projectId: string, userId: string): void {
    this.http.put<Project>(`/api/projects/${projectId}/users/${userId}`, {}).subscribe({
      next: (project) => this.replace(project),
      error: () => this.fail('Не удалось добавить пользователя'),
    });
  }

  removeMember(projectId: string, userId: string): void {
    this.http.delete<Project>(`/api/projects/${projectId}/users/${userId}`).subscribe({
      next: (project) => this.replace(project),
      error: () => this.fail('Не удалось удалить пользователя из проекта'),
    });
  }

  private replace(project: Project): void {
    this.projects.update((projects) =>
      projects.map((item) => (item.id === project.id ? project : item)).sort(this.byName),
    );
    this.saving.set(false);
  }

  private fail(message: string): void {
    this.error.set(message);
    this.loading.set(false);
    this.saving.set(false);
  }

  private readonly byName = (left: Project, right: Project) => left.name.localeCompare(right.name);
}
