import { Routes } from '@angular/router';
import { adminGuard, managerGuard } from './auth.guard';

export const routes: Routes = [
  {
    path: 'projects',
    loadComponent: () => import('./projects/projects-page').then((module) => module.ProjectsPage),
  },
  {
    path: 'projects/new',
    canActivate: [adminGuard],
    loadComponent: () => import('./projects/project-editor-page').then((module) => module.ProjectEditorPage),
  },
  {
    path: 'projects/:id/edit',
    canActivate: [adminGuard],
    loadComponent: () => import('./projects/project-editor-page').then((module) => module.ProjectEditorPage),
  },
  {
    path: 'projects/:id/relations',
    canActivate: [managerGuard],
    loadComponent: () => import('./projects/project-members-page').then((module) => module.ProjectMembersPage),
  },
  {
    path: 'users',
    loadComponent: () => import('./users/users-page').then((module) => module.UsersPage),
  },
  {
    path: 'users/new',
    canActivate: [adminGuard],
    loadComponent: () => import('./users/user-editor-page').then((module) => module.UserEditorPage),
  },
  {
    path: 'users/:id/edit',
    canActivate: [adminGuard],
    loadComponent: () => import('./users/user-editor-page').then((module) => module.UserEditorPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'projects' },
  { path: '**', redirectTo: 'projects' },
];
