import {
    Component, OnInit, Output, EventEmitter, signal, inject, Input
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { trigger, style, animate, transition } from '@angular/animations';
import { Transaction, CATEGORIES, CATEGORY_META, Category, Frequency, TransactionType, PaymentMethod } from '../../core/models/transaction.model';
import { TransactionService } from '../../core/services/transaction.service';

@Component({
    selector: 'app-transaction-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './transaction-form.component.html',
    styleUrl: './transaction-form.component.css',
    animations: [
        trigger('frequencySlide', [
            transition(':enter', [
                style({ opacity: 0, maxHeight: '0', overflow: 'hidden' }),
                animate('250ms ease', style({ opacity: 1, maxHeight: '80px' })),
            ]),
            transition(':leave', [
                animate('200ms ease', style({ opacity: 0, maxHeight: '0' })),
            ]),
        ]),
    ],
})
export class TransactionFormComponent implements OnInit {
    @Input() transaction?: Transaction;
    @Input() defaultType: TransactionType = 'EXPENSE';
    @Input() defaultDate?: string;
    @Output() transactionSaved = new EventEmitter<Transaction>();
    @Output() cancelled = new EventEmitter<void>();

    private readonly fb = inject(FormBuilder);
    private readonly transactionService = inject(TransactionService);

    readonly isSaving = signal<boolean>(false);
    readonly categories = CATEGORIES;
    readonly categoryMeta = CATEGORY_META;

    form!: FormGroup;

    get isRecurrentValue(): boolean {
        return !!this.form?.get('isRecurrent')?.value;
    }

    get selectedCategoryColor(): string {
        const cat = this.form?.get('category')?.value as Category | null;
        return cat ? CATEGORY_META[cat].color : 'var(--cat-assinaturas)';
    }

    ngOnInit(): void {
        const d = this.defaultDate ?? new Date().toISOString().split('T')[0];
        let type = 'SINGLE';
        if (this.transaction?.installmentsCount && this.transaction.installmentsCount > 1) {
            type = 'INSTALLMENT';
        } else if (this.transaction?.isRecurrent) {
            type = 'RECURRENT';
        }

        this.form = this.fb.group({
            type: [this.transaction?.type ?? this.defaultType, Validators.required],
            paymentMethod: [this.transaction?.paymentMethod ?? 'DEBIT', Validators.required],
            description: [this.transaction?.description ?? '', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
            amount: [this.transaction?.amount ?? null, [Validators.required, Validators.min(0.01)]],
            category: [this.transaction?.category ?? (this.defaultType === 'INCOME' ? 'RECEITA' : 'ASSINATURAS'), Validators.required],
            transactionDate: [this.transaction ? this.transaction.transactionDate.substring(0, 10) : d, Validators.required],
            transactionType: [type, Validators.required],
            frequency: [this.transaction?.frequency ?? 'MONTHLY'],
            installmentsCount: [this.transaction?.installmentsCount ?? 2, [Validators.min(2), Validators.max(120)]]
        });
    }

    onAmountBlur(): void {
        const ctrl = this.form.get('amount');
        if (ctrl?.value) {
            ctrl.setValue(parseFloat(ctrl.value).toFixed(2));
        }
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const raw = this.form.value;
        const type = raw['transactionType'];
        const payload = {
            type: raw['type'] as TransactionType,
            paymentMethod: raw['paymentMethod'] as PaymentMethod,
            description: raw['description'] as string,
            amount: parseFloat(raw['amount']),
            category: raw['category'] as Category,
            transactionDate: raw['transactionDate'] as string,
            isRecurrent: type === 'RECURRENT',
            frequency: type === 'RECURRENT' ? (raw['frequency'] as Frequency) : undefined,
            installmentsCount: type === 'INSTALLMENT' ? parseInt(raw['installmentsCount'], 10) : undefined,
        };

        this.isSaving.set(true);
        const obs = this.transaction
            ? this.transactionService.update(this.transaction.id, payload)
            : this.transactionService.create(payload);

        obs.subscribe({
            next: transaction => {
                this.isSaving.set(false);
                this.transactionSaved.emit(transaction);
            },
            error: () => this.isSaving.set(false),
        });
    }

    onCancel(): void {
        this.cancelled.emit();
    }

    hasError(field: string, error: string): boolean {
        const ctrl = this.form.get(field);
        return !!(ctrl?.hasError(error) && ctrl?.touched);
    }

    isTouched(field: string): boolean {
        return !!this.form.get(field)?.touched;
    }

    isInvalid(field: string): boolean {
        const ctrl = this.form.get(field);
        return !!(ctrl?.invalid && ctrl?.touched);
    }
}

