import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, User } from '../../core/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
})
export class Home {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly username = this.auth.username;
  protected readonly user = signal<User | null>(null);

  constructor() {
    this.auth.getCurrentUser().subscribe((u) => this.user.set(u));
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
