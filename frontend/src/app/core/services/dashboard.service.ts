import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary, MonthlyTrend } from '../models/dashboard.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DashboardService {
    private readonly baseUrl = `${environment.apiUrl}/dashboard`;

    constructor(private readonly http: HttpClient) { }

    getSummary(): Observable<DashboardSummary> {
        return this.http.get<DashboardSummary>(`${this.baseUrl}/summary`);
    }

    getMonthlyTrend(year?: number): Observable<MonthlyTrend[]> {
        const params = year ? { params: { year: year.toString() } } : {};
        return this.http.get<MonthlyTrend[]>(`${this.baseUrl}/monthly-trend`, params);
    }
}
