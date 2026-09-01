import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';

import { AuthStore } from '../auth.store';

@Component({
  selector: 'app-profile-page',
  imports: [DatePipe, MatButtonModule, MatCardModule, RouterLink],
  templateUrl: './profile-page.html',
  styleUrl: './profile-page.scss',
})
export class ProfilePage {
  protected readonly auth = inject(AuthStore);
}
