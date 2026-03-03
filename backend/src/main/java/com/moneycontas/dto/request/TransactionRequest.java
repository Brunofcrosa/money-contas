package com.moneycontas.dto.request;

import com.moneycontas.domain.enums.Category;
import com.moneycontas.domain.enums.Frequency;
import com.moneycontas.domain.enums.PaymentMethod;
import com.moneycontas.domain.enums.TransactionType;
import com.moneycontas.validation.ValidRecurrence;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@ValidRecurrence
public record TransactionRequest(

        @NotBlank(message = "description is required") @Size(max = 500, message = "description must not exceed 500 characters") String description,

        @NotNull(message = "amount is required") @Positive(message = "amount must be positive") @Digits(integer = 10, fraction = 2, message = "amount must have at most 10 integer digits and 2 decimal places") BigDecimal amount,

        @NotNull(message = "category is required") Category category,

        @NotNull(message = "transactionDate is required") LocalDate transactionDate,

        boolean isRecurrent,

        Frequency frequency,

        @Positive(message = "installments count must be positive") Integer installmentsCount,

        TransactionType type,

        PaymentMethod paymentMethod

) {
}
