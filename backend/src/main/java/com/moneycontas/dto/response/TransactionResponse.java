package com.moneycontas.dto.response;

import com.moneycontas.domain.entity.Transaction;
import com.moneycontas.domain.enums.Category;
import com.moneycontas.domain.enums.Frequency;
import com.moneycontas.domain.enums.PaymentMethod;
import com.moneycontas.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        Category category,
        LocalDate transactionDate,
        boolean isRecurrent,
        Frequency frequency,
        Integer installmentsCount,
        Integer installmentNumber,
        TransactionType type,
        PaymentMethod paymentMethod,
        LocalDateTime createdAt) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getCategory(),
                t.getTransactionDate(),
                Boolean.TRUE.equals(t.getIsRecurrent()),
                t.getFrequency(),
                t.getInstallmentsCount(),
                t.getInstallmentNumber(),
                t.getType(),
                t.getPaymentMethod(),
                t.getCreatedAt());
    }
}
