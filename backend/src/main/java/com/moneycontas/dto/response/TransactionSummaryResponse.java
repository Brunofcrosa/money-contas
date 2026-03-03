package com.moneycontas.dto.response;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        BigDecimal currentBalance,
        BigDecimal monthIncome,
        BigDecimal monthExpense,
        BigDecimal monthCredit,
        BigDecimal monthDebit) {
}
