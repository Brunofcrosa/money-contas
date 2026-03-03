import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function passwordMatchValidator(control: AbstractControl) {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (!password || !confirmPassword) return null;
    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
}

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './register.component.html',
})
export class RegisterComponent {
    readonly isLoading = signal(false);
    readonly errorMsg = signal<string | null>(null);
    readonly showPass = signal(false);

    readonly form: FormGroup;

    constructor(
        private readonly fb: FormBuilder,
        private readonly authService: AuthService,
        private readonly router: Router
    ) {
        this.form = this.fb.group(
            {
                name: ['', [Validators.required, Validators.minLength(2)]],
                email: ['', [Validators.required, Validators.email]],
                password: ['', [Validators.required, Validators.minLength(6)]],
                confirmPassword: ['', Validators.required],
            },
            { validators: passwordMatchValidator }
        );
    }

    togglePassword(): void {
        this.showPass.update(v => !v);
    }

    onSubmit(): void {
        if (this.form.invalid || this.isLoading()) return;

        this.isLoading.set(true);
        this.errorMsg.set(null);

        const { name, email, password } = this.form.getRawValue();
        this.authService.register({ name, email, password }).subscribe({
            next: () => this.router.navigate(['/dashboard']),
            error: (err) => {
                const msg = err?.error?.message ?? 'Erro ao criar conta. Tente novamente.';
                this.errorMsg.set(msg);
                this.isLoading.set(false);
            },
        });
    }

    get nameCtrl() { return this.form.get('name'); }
    get emailCtrl() { return this.form.get('email'); }
    get passwordCtrl() { return this.form.get('password'); }
    get confirmCtrl() { return this.form.get('confirmPassword'); }
}
