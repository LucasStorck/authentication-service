import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
}

export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

const ACCESS_TOKEN_KEY = 'access_token';
const USERNAME_KEY = 'username';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly token = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));
  readonly username = signal<string | null>(localStorage.getItem(USERNAME_KEY));
  readonly isLoggedIn = computed(() => this.token() !== null);

  get accessToken(): string | null {
    return this.token();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/login', { username, password }).pipe(
      tap((res) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, res.accessToken);
        localStorage.setItem(USERNAME_KEY, username);
        this.token.set(res.accessToken);
        this.username.set(username);
      }),
    );
  }

  register(user: CreateUserRequest): Observable<void> {
    return this.http.post<void>('/api/user', user);
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`/api/user/${encodeURIComponent(this.username() ?? '')}`);
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    this.token.set(null);
    this.username.set(null);
  }
}
