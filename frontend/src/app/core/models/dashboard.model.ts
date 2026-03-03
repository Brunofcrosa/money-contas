import { Category } from './transaction.model';

export interface DashboardSummary {
    totalByCategory: Record<Category, number>;
    grandTotal: number;
    transactionCount: number;
}

export interface MonthlyTrend {
    month: string;
    total: number;
}
