import { Routes } from '@angular/router';
import { adminGuard, managerGuard, userGuard } from './auth.guard';

export const routes: Routes = [
  {
    path: 'projects',
    loadComponent: () => import('./projects/projects-page').then((module) => module.ProjectsPage),
  },
  {
    path: 'projects/new',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./projects/project-editor-page').then((module) => module.ProjectEditorPage),
  },
  {
    path: 'projects/:id/edit',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./projects/project-editor-page').then((module) => module.ProjectEditorPage),
  },
  {
    path: 'projects/:id/relations',
    canActivate: [managerGuard],
    loadComponent: () =>
      import('./projects/project-members-page').then((module) => module.ProjectMembersPage),
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
  {
    path: 'profile',
    loadComponent: () => import('./profile/profile-page').then((module) => module.ProfilePage),
  },
  {
    path: 'profile/vacation-requests',
    loadComponent: () =>
      import('./vacation-requests/vacation-requests-page').then(
        (module) => module.VacationRequestsPage,
      ),
  },
  {
    path: 'profile/vacation-requests/new',
    canActivate: [userGuard],
    loadComponent: () =>
      import('./vacation-requests/vacation-request-editor-page').then(
        (module) => module.VacationRequestEditorPage,
      ),
  },
  {
    path: 'profile/vacation-requests/:id/edit',
    canActivate: [userGuard],
    loadComponent: () =>
      import('./vacation-requests/vacation-request-editor-page').then(
        (module) => module.VacationRequestEditorPage,
      ),
  },
  {
    path: 'profile/vacation-requests/review',
    canActivate: [managerGuard],
    loadComponent: () =>
      import('./manager-vacation-requests/manager-vacation-requests-page').then(
        (module) => module.ManagerVacationRequestsPage,
      ),
  },
  {
    path: 'profile/vacation-requests/review/schedule',
    canActivate: [managerGuard],
    loadComponent: () =>
      import('./manager-vacation-requests/manager-vacation-request-schedule-page').then(
        (module) => module.ManagerVacationRequestSchedulePage,
      ),
  },
  {
    path: 'profile/vacation-requests/review/:id',
    canActivate: [managerGuard],
    loadComponent: () =>
      import('./manager-vacation-requests/manager-vacation-request-review-page').then(
        (module) => module.ManagerVacationRequestReviewPage,
      ),
  },
  { path: '', pathMatch: 'full', redirectTo: 'projects' },
  { path: '**', redirectTo: 'projects' },
];
