import { Component, OnInit, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';

import { UsersStore } from './users.store';

@Component({
  selector: 'app-users-page',
  imports: [MatCardModule, MatProgressSpinnerModule, MatTableModule],
  templateUrl: './users-page.html',
  styleUrl: './users-page.scss',
})
export class UsersPage implements OnInit {
  protected readonly store = inject(UsersStore);
  protected readonly columns = ['displayName', 'email'];

  ngOnInit(): void {
    this.store.load();
  }
}
