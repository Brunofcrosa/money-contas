import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Transaction, CATEGORY_META } from '../../core/models/transaction.model';

@Component({
    selector: 'app-transaction-item',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './transaction-item.component.html',
    styleUrl: './transaction-item.component.css',
})
export class TransactionItemComponent {
    @Input({ required: true }) transaction!: Transaction;
    @Output() edit = new EventEmitter<Transaction>();
    @Output() delete = new EventEmitter<Transaction>();

    get meta() {
        return CATEGORY_META[this.transaction.category];
    }

    get isIncome(): boolean {
        return this.transaction.type === 'INCOME';
    }

    get formattedAmount(): string {
        const sign = this.isIncome ? '+ ' : '- ';
        return sign + new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(this.transaction.amount);
    }

    get formattedDate(): string {
        return new Intl.DateTimeFormat('pt-BR').format(
            new Date(this.transaction.transactionDate + 'T00:00:00')
        );
    }

    get frequencyLabel(): string {
        if (!this.transaction.frequency) return '';
        return this.transaction.frequency === 'MONTHLY' ? 'Mensal' : 'Anual';
    }
}
