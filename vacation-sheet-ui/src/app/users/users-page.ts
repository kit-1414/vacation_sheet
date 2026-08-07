import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { UsersStore } from './users.store';

@Component({
  selector: 'app-users-page',
  imports: [DatePipe, MatButtonModule, MatCardModule, MatProgressSpinnerModule, MatTableModule, RouterLink],
  templateUrl: './users-page.html',
  styleUrl: './users-page.scss',
})
export class UsersPage implements OnInit {
  protected readonly store = inject(UsersStore);
  protected readonly columns = ['lastName', 'firstName', 'email', 'ctime', 'utime', 'edit'];

  ngOnInit(): void {
    this.store.load();
  }
}
