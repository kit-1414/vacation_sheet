import { DatePipe, SlicePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';

import { UserAccount } from '../users/users.store';
import { Project, ProjectsStore } from './projects.store';

@Component({
  selector: 'app-projects-page',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    DatePipe,
    SlicePipe,
    RouterLink,
  ],
  templateUrl: './projects-page.html',
  styleUrl: './projects-page.scss',
})
export class ProjectsPage implements OnInit {
  protected readonly store = inject(ProjectsStore);

  ngOnInit(): void {
    this.store.load();
  }

  protected availableUsers(project: Project) {
    const memberIds = new Set(project.members.map((member) => member.id));
    return this.store.users().filter((user) => !memberIds.has(user.id));
  }

  protected userName(user: UserAccount): string {
    return [user.firstName, user.lastName].filter(Boolean).join(' ') || user.email;
  }
}
