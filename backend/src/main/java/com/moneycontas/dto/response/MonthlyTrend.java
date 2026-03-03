package com.moneycontas.dto.response;

import java.math.BigDecimal;


public record MonthlyTrend(String month, BigDecimal total) {
}
