package com.moneycontas.dto.response;

import java.math.BigDecimal;
import java.util.Map;


public record DashboardSummaryResponse(
        Map<String, BigDecimal> totalByCategory,
        BigDecimal grandTotal,
        long transactionCount,
        BigDecimal recurrentMonthlyTotal) {
}
