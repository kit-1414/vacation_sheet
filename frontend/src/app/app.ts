import { Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';

import { AuthStore } from './auth.store';

@Component({
  selector: 'app-root',
  imports: [MatButtonModule, MatCardModule, MatProgressSpinnerModule, MatToolbarModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  protected readonly auth = inject(AuthStore);

  ngOnInit(): void {
    this.auth.load();
  }
}
