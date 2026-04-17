import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthResponse { token: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  readonly token = signal<string | null>(localStorage.getItem('jwt'));

  login(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(tap(res => this.storeToken(res.token)));
  }

  register(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/register`, { email, password })
      .pipe(tap(res => this.storeToken(res.token)));
  }

  logout() {
    localStorage.removeItem('jwt');
    this.token.set(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated() {
    return this.token() !== null;
  }

  private storeToken(token: string) {
    localStorage.setItem('jwt', token);
    this.token.set(token);
  }
}
