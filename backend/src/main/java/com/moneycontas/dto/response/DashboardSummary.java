package com.moneycontas.dto.response;

import com.moneycontas.domain.enums.Category;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummary(
        Map<Category, BigDecimal> totalByCategory,
        BigDecimal grandTotal,
        long transactionCount) {
}
