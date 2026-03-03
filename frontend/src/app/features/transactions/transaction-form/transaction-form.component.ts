import {
    Component,
    OnInit,
    input,
    output,
    signal,
    computed,
    inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Transaction, Category, Frequency, ALL_CATEGORIES, CATEGORY_LABELS, FREQUENCY_LABELS } from '../../../core/models/transaction.model';
import { TransactionService } from '../../../core/services/transaction.service';

@Component({
    selector: 'app-transaction-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './transaction-form.component.html',
})
export class TransactionFormComponent implements OnInit {
    private readonly transactionService = inject(TransactionService);

    
    readonly transaction = input<Transaction | null>(null);

    readonly saved = output<void>();
    readonly cancelled = output<void>();

    readonly isLoading = signal(false);
    readonly errorMsg = signal<string | null>(null);

    readonly isEditMode = computed(() => !!this.transaction());

    readonly allCategories = ALL_CATEGORIES;
    readonly categoryLabels = CATEGORY_LABELS;
    readonly frequencies: Frequency[] = ['MONTHLY', 'ANNUAL'];
    readonly frequencyLabels = FREQUENCY_LABELS;

    readonly form: FormGroup;

    constructor(private readonly fb: FormBuilder) {
        this.form = this.fb.group({
            description: ['', [Validators.required, Validators.minLength(2)]],
            amount: [null, [Validators.required, Validators.min(0.01)]],
            category: ['ALIMENTACAO', Validators.required],
            transactionDate: ['', Validators.required],
            isRecurrent: [false],
            frequency: ['MONTHLY'],
        });
    }

    ngOnInit(): void {
        const tx = this.transaction();
        if (tx) {
            this.form.patchValue({
                description: tx.description,
                amount: tx.amount,
                category: tx.category,
                transactionDate: tx.transactionDate.substring(0, 10),
                isRecurrent: tx.isRecurrent,
                frequency: tx.frequency ?? 'MONTHLY',
            });
        } else {
            
            const today = new Date().toISOString().substring(0, 10);
            this.form.patchValue({ transactionDate: today });
        }
    }

    get showFrequency(): boolean {
        return this.form.get('isRecurrent')?.value === true;
    }

    onSubmit(): void {
        if (this.form.invalid || this.isLoading()) return;
        this.isLoading.set(true);
        this.errorMsg.set(null);

        const raw = this.form.getRawValue();
        const payload = {
            description: raw.description,
            amount: Number(raw.amount),
            category: raw.category as Category,
            transactionDate: raw.transactionDate,
            isRecurrent: raw.isRecurrent,
            frequency: raw.isRecurrent ? raw.frequency as Frequency : undefined,
        };

        const tx = this.transaction();
        const obs = tx
            ? this.transactionService.update(tx.id, payload)
            : this.transactionService.create(payload);

        obs.subscribe({
            next: () => { this.isLoading.set(false); this.saved.emit(); },
            error: (err) => {
                const msg = err?.error?.message ?? 'Erro ao salvar. Tente novamente.';
                this.errorMsg.set(msg);
                this.isLoading.set(false);
            },
        });
    }

    onCancel(): void {
        this.cancelled.emit();
    }

    get descriptionCtrl() { return this.form.get('description'); }
    get amountCtrl() { return this.form.get('amount'); }
    get categoryCtrl() { return this.form.get('category'); }
    get transactionDateCtrl() { return this.form.get('transactionDate'); }
}
