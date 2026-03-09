import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, CreateTransactionRequest, UpdateTransactionRequest, Category, TransactionSummary } from '../models/transaction.model';
import { environment } from '../../../environments/environment';

export interface TransactionFilters {
    category?: Category;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
}

export interface PagedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {
    private readonly baseUrl = `${environment.apiUrl}/transactions`;

    constructor(private readonly http: HttpClient) { }

    getAll(filters?: TransactionFilters): Observable<PagedResponse<Transaction>> {
        let params = new HttpParams();
        if (filters?.category) params = params.set('category', filters.category);
        if (filters?.startDate) params = params.set('startDate', filters.startDate);
        if (filters?.endDate) params = params.set('endDate', filters.endDate);
        if (filters?.page != null) params = params.set('page', filters.page.toString());
        if (filters?.size != null) params = params.set('size', filters.size.toString());

        return this.http.get<PagedResponse<Transaction>>(this.baseUrl, { params });
    }

    getById(id: string): Observable<Transaction> {
        return this.http.get<Transaction>(`${this.baseUrl}/${id}`);
    }

    getSummary(startDate: string, endDate: string): Observable<TransactionSummary> {
        let params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);
        return this.http.get<TransactionSummary>(`${this.baseUrl}/summary`, { params });
    }

    carryOverToNextMonth(startDate: string, endDate: string): Observable<Transaction> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);
        return this.http.post<Transaction>(`${this.baseUrl}/carry-over`, null, { params });
    }

    create(data: CreateTransactionRequest): Observable<Transaction> {
        return this.http.post<Transaction>(this.baseUrl, data);
    }

    update(id: string, data: UpdateTransactionRequest): Observable<Transaction> {
        return this.http.put<Transaction>(`${this.baseUrl}/${id}`, data);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}
