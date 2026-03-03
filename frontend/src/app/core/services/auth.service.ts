import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '../models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY = 'mc_token';
    private readonly USER_KEY = 'mc_user';

    private readonly _token = signal<string | null>(this.loadToken());
    private readonly _currentUser = signal<UserProfile | null>(this.loadUser());

    readonly isAuthenticated = computed(() => !!this._token());
    readonly currentUser = computed(() => this._currentUser());

    constructor(
        private readonly http: HttpClient,
        private readonly router: Router
    ) { }

    login(credentials: LoginRequest): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials)
            .pipe(tap(res => this.saveSession(res)));
    }

    register(data: RegisterRequest): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${environment.apiUrl}/auth/register`, data)
            .pipe(tap(res => this.saveSession(res)));
    }

    logout(): void {
        this.clearSession();
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return this._token();
    }

    clearSession(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this._token.set(null);
        this._currentUser.set(null);
    }

    private saveSession(res: AuthResponse): void {
        const profile: UserProfile = { id: res.userId, name: res.name, email: res.email };
        localStorage.setItem(this.TOKEN_KEY, res.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(profile));
        this._token.set(res.token);
        this._currentUser.set(profile);
    }

    private loadToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    private loadUser(): UserProfile | null {
        const raw = localStorage.getItem(this.USER_KEY);
        if (!raw) return null;
        try {
            return JSON.parse(raw) as UserProfile;
        } catch {
            return null;
        }
    }
}
