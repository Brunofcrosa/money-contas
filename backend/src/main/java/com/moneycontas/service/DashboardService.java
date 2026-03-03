package com.moneycontas.service;

import com.moneycontas.domain.entity.Transaction;
import com.moneycontas.domain.entity.User;
import com.moneycontas.domain.enums.Frequency;
import com.moneycontas.dto.response.DashboardSummaryResponse;
import com.moneycontas.dto.response.TransactionResponse;
import com.moneycontas.repository.TransactionRepository;
import com.moneycontas.repository.TransactionRepository.CategoryTotal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(User currentUser) {
        List<CategoryTotal> categoryTotals = transactionRepository.sumAmountByCategoryForUser(currentUser.getId());

        Map<String, BigDecimal> totalByCategory = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        long transactionCount = transactionRepository.countByUserId(currentUser.getId());

        for (CategoryTotal ct : categoryTotals) {
            BigDecimal total = ct.getTotal().setScale(2, RoundingMode.HALF_UP);
            totalByCategory.put(ct.getCategory().name(), total);
            grandTotal = grandTotal.add(total);
        }

        
        List<Transaction> recurrentTransactions = transactionRepository
                .findByUserIdAndIsRecurrentTrue(currentUser.getId());
        BigDecimal recurrentMonthlyTotal = BigDecimal.ZERO;

        for (Transaction t : recurrentTransactions) {
            if (t.getFrequency() == Frequency.MONTHLY) {
                recurrentMonthlyTotal = recurrentMonthlyTotal.add(t.getAmount());
            } else if (t.getFrequency() == Frequency.ANNUAL) {
                recurrentMonthlyTotal = recurrentMonthlyTotal.add(
                        t.getAmount().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));
            }
        }

        return new DashboardSummaryResponse(
                totalByCategory,
                grandTotal.setScale(2, RoundingMode.HALF_UP),
                transactionCount,
                recurrentMonthlyTotal.setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecurrentTransactions(User currentUser) {
        return transactionRepository.findByUserIdAndIsRecurrentTrue(currentUser.getId())
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
