import { Component, OnInit, signal, computed, effect, inject } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { NgChartsModule } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

import {
    Transaction, Category, CATEGORIES, CATEGORY_META, CATEGORY_LABELS, CATEGORY_COLORS_HEX, TransactionSummary, TransactionType
} from '../../core/models/transaction.model';
import { TransactionService } from '../../core/services/transaction.service';
import { AuthService } from '../../core/services/auth.service';
import { SummaryCardComponent } from '../../shared/summary-card/summary-card.component';
import { TransactionItemComponent } from '../../shared/transaction-item/transaction-item.component';
import { TransactionFormComponent } from '../../shared/transaction-form/transaction-form.component';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        NgChartsModule,
        SummaryCardComponent,
        TransactionItemComponent,
        TransactionFormComponent,
    ],
    templateUrl: './dashboard.component.html',
    styleUrl: './dashboard.component.css',
    animations: [
        trigger('fadeSlideUp', [
            transition(':enter', [
                style({ opacity: 0, transform: 'translateY(60px)' }),
                animate('300ms cubic-bezier(0.16,1,0.3,1)', style({ opacity: 1, transform: 'translateY(0)' })),
            ]),
            transition(':leave', [
                animate('200ms ease-in', style({ opacity: 0, transform: 'translateY(40px)' })),
            ]),
        ]),
        trigger('overlayFade', [
            transition(':enter', [
                style({ opacity: 0 }),
                animate('200ms ease', style({ opacity: 1 })),
            ]),
            transition(':leave', [
                animate('180ms ease', style({ opacity: 0 })),
            ]),
        ]),
    ],
})
export class DashboardComponent implements OnInit {
    private readonly transactionService = inject(TransactionService);
    readonly authService = inject(AuthService);

    
    readonly transactions = signal<Transaction[]>([]);
    readonly summary = signal<TransactionSummary | null>(null);
    readonly selectedCategory = signal<Category | null>(null);
    readonly isLoading = signal<boolean>(false);
    readonly isModalOpen = signal<boolean>(false);
    readonly isSaving = signal<boolean>(false);
    readonly currentMonth = signal<Date>(new Date());
    readonly transactionTypeToCreate = signal<TransactionType>('EXPENSE');

    
    readonly editingTransaction = signal<Transaction | undefined>(undefined);
    readonly confirmDeleteId = signal<string | null>(null);

    
    readonly filteredTransactions = computed(() => {
        const cat = this.selectedCategory();
        return cat
            ? this.transactions().filter(t => t.category === cat)
            : this.transactions();
    });

    readonly categoryTotals = computed(() =>
        this.transactions().reduce((acc, t) => {
            acc[t.category] = (acc[t.category] ?? 0) + t.amount;
            return acc;
        }, {} as Record<Category, number>)
    );

    readonly totalExpenses = computed(() => {
        const s = this.summary();
        if (s) return s.monthExpense;
        return this.transactions().filter(t => t.type === 'EXPENSE').reduce((sum, t) => sum + t.amount, 0);
    });

    readonly monthIncome = computed(() => this.summary()?.monthIncome ?? 0);
    readonly monthCredit = computed(() => this.summary()?.monthCredit ?? 0);
    readonly monthDebit = computed(() => this.summary()?.monthDebit ?? 0);
    readonly currentBalance = computed(() => this.summary()?.currentBalance ?? 0);

    readonly recurrentExpenses = computed(() =>
        this.transactions().filter(t => t.isRecurrent).reduce((sum, t) => sum + t.amount, 0)
    );

    readonly chartData = computed((): ChartData<'doughnut'> => ({
        labels: CATEGORIES.map(c => CATEGORY_META[c].label),
        datasets: [{
            data: CATEGORIES.map(c => this.categoryTotals()[c] ?? 0),
            backgroundColor: CATEGORIES.map(c => CATEGORY_META[c].color + 'cc'),
            borderColor: CATEGORIES.map(c => CATEGORY_META[c].color),
            borderWidth: 2,
            hoverOffset: 8,
        }],
    }));

    readonly chartOptions: ChartOptions<'doughnut'> = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '70%',
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                callbacks: {
                    label: ctx => {
                        const val = ctx.raw as number;
                        return ` ${CATEGORIES[ctx.dataIndex] ? CATEGORY_META[CATEGORIES[ctx.dataIndex]].label : ''}: ${this.formatCurrency(val)}`;
                    },
                },
            },
        },
    };

    readonly categories = CATEGORIES;
    readonly categoryMeta = CATEGORY_META;

    readonly greeting = computed(() => {
        const hour = new Date().getHours();
        if (hour < 12) return 'Bom dia';
        if (hour < 18) return 'Boa tarde';
        return 'Boa noite';
    });

    readonly todayFormatted = computed(() =>
        new Intl.DateTimeFormat('pt-BR', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
        }).format(new Date())
    );

    readonly monthsList = [
        { value: 0, label: 'Janeiro' },
        { value: 1, label: 'Fevereiro' },
        { value: 2, label: 'Março' },
        { value: 3, label: 'Abril' },
        { value: 4, label: 'Maio' },
        { value: 5, label: 'Junho' },
        { value: 6, label: 'Julho' },
        { value: 7, label: 'Agosto' },
        { value: 8, label: 'Setembro' },
        { value: 9, label: 'Outubro' },
        { value: 10, label: 'Novembro' },
        { value: 11, label: 'Dezembro' }
    ];

    readonly availableYears = Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - 3 + i);

    readonly currentYear = computed(() => this.currentMonth().getFullYear());
    readonly currentMonthIndex = computed(() => this.currentMonth().getMonth());

    readonly userInitial = computed(() => {
        const name = this.authService.currentUser()?.name ?? 'U';
        return name.charAt(0).toUpperCase();
    });

    readonly userName = computed(() =>
        this.authService.currentUser()?.name ?? 'Usuário'
    );

    constructor() {
        
        effect(() => {
            this.chartData(); 
        });
    }

    ngOnInit(): void {
        this.loadTransactions();
    }

    loadTransactions(): void {
        this.isLoading.set(true);
        const y = this.currentMonth().getFullYear();
        const m = this.currentMonth().getMonth();
        const start = new Date(y, m, 1).toLocaleDateString("en-CA"); 
        const end = new Date(y, m + 1, 0).toLocaleDateString("en-CA");

        forkJoin({
            txs: this.transactionService.getAll({ size: 200, startDate: start, endDate: end }),
            sum: this.transactionService.getSummary(start, end)
        }).subscribe({
            next: res => {
                this.transactions.set(res.txs.content);
                this.summary.set(res.sum);
                this.isLoading.set(false);
            },
            error: () => this.isLoading.set(false),
        });
    }

    prevMonth(): void {
        const d = this.currentMonth();
        this.currentMonth.set(new Date(d.getFullYear(), d.getMonth() - 1, 1));
        this.loadTransactions();
    }

    nextMonth(): void {
        const d = this.currentMonth();
        this.currentMonth.set(new Date(d.getFullYear(), d.getMonth() + 1, 1));
        this.loadTransactions();
    }

    onMonthSelect(event: Event): void {
        const val = parseInt((event.target as HTMLSelectElement).value, 10);
        if (isNaN(val)) return;
        const y = this.currentMonth().getFullYear();
        this.currentMonth.set(new Date(y, val, 1));
        this.loadTransactions();
    }

    onYearSelect(event: Event): void {
        const val = parseInt((event.target as HTMLSelectElement).value, 10);
        if (isNaN(val)) return;
        const m = this.currentMonth().getMonth();
        this.currentMonth.set(new Date(val, m, 1));
        this.loadTransactions();
    }

    onCategorySelected(cat: Category): void {
        this.selectedCategory.update(prev => (prev === cat ? null : cat));
    }

    openModal(type: TransactionType = 'EXPENSE'): void {
        this.transactionTypeToCreate.set(type);
        this.isModalOpen.set(true);
        document.body.style.overflow = 'hidden';
    }

    closeModal(): void {
        this.isModalOpen.set(false);
        this.editingTransaction.set(undefined);
        document.body.style.overflow = '';
    }

    onTransactionSaved(transaction: Transaction): void {
        this.loadTransactions(); 
        this.closeModal();
    }

    openEdit(tx: Transaction): void {
        this.editingTransaction.set(tx);
        this.openModal();
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
        this.isSaving.set(true);
        this.transactionService.delete(id).subscribe({
            next: () => {
                this.transactions.update(list => list.filter(t => t.id !== id));
                this.isSaving.set(false);
                this.confirmDeleteId.set(null);
            },
            error: () => this.isSaving.set(false),
        });
    }

    onLogout(): void {
        this.authService.logout();
    }

    formatCurrency(value: number): string {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
    }

    formatDate(dateStr: string): string {
        return new Intl.DateTimeFormat('pt-BR').format(new Date(dateStr + 'T00:00:00'));
    }

    getCategoryCount(cat: Category): number {
        return this.transactions().filter(t => t.category === cat).length;
    }

    trackById(_: number, item: Transaction): string {
        return item.id;
    }
}
