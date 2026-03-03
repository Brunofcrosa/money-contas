import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './login.component.html',
})
export class LoginComponent {
    readonly isLoading = signal(false);
    readonly errorMsg = signal<string | null>(null);
    readonly showPass = signal(false);

    readonly form: FormGroup;

    constructor(
        private readonly fb: FormBuilder,
        private readonly authService: AuthService,
        private readonly router: Router
    ) {
        this.form = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
        });
    }

    togglePassword(): void {
        this.showPass.update(v => !v);
    }

    onSubmit(): void {
        if (this.form.invalid || this.isLoading()) return;

        this.isLoading.set(true);
        this.errorMsg.set(null);

        this.authService.login(this.form.getRawValue()).subscribe({
            next: () => this.router.navigate(['/dashboard']),
            error: (err) => {
                const msg = err?.error?.message ?? 'E-mail ou senha inválidos.';
                this.errorMsg.set(msg);
                this.isLoading.set(false);
            },
        });
    }

    get emailCtrl() { return this.form.get('email'); }
    get passwordCtrl() { return this.form.get('password'); }
}
