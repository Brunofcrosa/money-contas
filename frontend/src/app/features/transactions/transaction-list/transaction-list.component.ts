import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Transaction, Category, ALL_CATEGORIES, CATEGORY_LABELS } from '../../../core/models/transaction.model';
import { TransactionService } from '../../../core/services/transaction.service';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryBadgeComponent } from '../../../shared/components/category-badge/category-badge.component';
import { TransactionFormComponent } from '../transaction-form/transaction-form.component';

@Component({
    selector: 'app-transaction-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        RouterLinkActive,
        CategoryBadgeComponent,
        TransactionFormComponent,
    ],
    templateUrl: './transaction-list.component.html',
})
export class TransactionListComponent implements OnInit {
    private readonly transactionService = inject(TransactionService);
    readonly authService = inject(AuthService);

    readonly transactions = signal<Transaction[]>([]);
    readonly isLoading = signal<boolean>(false);
    readonly filterCategory = signal<Category | null>(null);
    readonly searchQuery = signal<string>('');
    readonly showForm = signal<boolean>(false);
    readonly editingTransaction = signal<Transaction | null>(null);
    readonly isSidebarOpen = signal<boolean>(false);
    readonly confirmDeleteId = signal<string | null>(null);
    readonly isDeleting = signal<boolean>(false);

    readonly filteredTransactions = computed(() => {
        const cat = this.filterCategory();
        const query = this.searchQuery().toLowerCase();
        return this.transactions().filter(t => {
            const matchCat = cat ? t.category === cat : true;
            const matchQuery = query ? t.description.toLowerCase().includes(query) : true;
            return matchCat && matchQuery;
        });
    });

    readonly totalFiltered = computed(() =>
        this.filteredTransactions().reduce((sum, t) => sum + t.amount, 0)
    );

    readonly allCategories = ALL_CATEGORIES;
    readonly categoryLabels = CATEGORY_LABELS;

    ngOnInit(): void {
        this.loadTransactions();
    }

    loadTransactions(): void {
        this.isLoading.set(true);
        this.transactionService.getAll({ size: 200 }).subscribe({
            next: res => { this.transactions.set(res.content); this.isLoading.set(false); },
            error: () => this.isLoading.set(false),
        });
    }

    setFilter(cat: Category | null): void {
        this.filterCategory.set(cat);
    }

    onSearch(value: string): void {
        this.searchQuery.set(value);
    }

    openCreate(): void {
        this.editingTransaction.set(null);
        this.showForm.set(true);
    }

    openEdit(tx: Transaction): void {
        this.editingTransaction.set(tx);
        this.showForm.set(true);
    }

    closeForm(): void {
        this.showForm.set(false);
        this.editingTransaction.set(null);
    }

    onFormSaved(): void {
        this.closeForm();
        this.loadTransactions();
    }

    requestDelete(id: string): void {
        this.confirmDeleteId.set(id);
    }

    cancelDelete(): void {
        this.confirmDeleteId.set(null);
    }

    confirmDelete(): void {
        const id = this.confirmDeleteId();
        if (!id) return;
        this.isDeleting.set(true);
        this.transactionService.delete(id).subscribe({
            next: () => {
                this.transactions.update(list => list.filter(t => t.id !== id));
                this.confirmDeleteId.set(null);
                this.isDeleting.set(false);
            },
            error: () => this.isDeleting.set(false),
        });
    }

    toggleSidebar(): void {
        this.isSidebarOpen.update(v => !v);
    }

    formatCurrency(value: number): string {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
    }

    formatDate(dateStr: string): string {
        return new Intl.DateTimeFormat('pt-BR').format(new Date(dateStr));
    }
}
