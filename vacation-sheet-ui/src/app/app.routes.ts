import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'projects',
    loadComponent: () => import('./projects/projects-page').then((module) => module.ProjectsPage),
  },
  {
    path: 'users',
    loadComponent: () => import('./users/users-page').then((module) => module.UsersPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'projects' },
  { path: '**', redirectTo: 'projects' },
];
