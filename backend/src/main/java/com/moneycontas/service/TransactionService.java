package com.moneycontas.service;

import com.moneycontas.domain.entity.Transaction;
import com.moneycontas.domain.entity.User;
import com.moneycontas.domain.enums.Category;
import com.moneycontas.dto.request.TransactionRequest;
import com.moneycontas.dto.response.TransactionResponse;
import com.moneycontas.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final com.moneycontas.repository.UserBalanceRepository userBalanceRepository;

    public TransactionService(TransactionRepository transactionRepository,
            com.moneycontas.repository.UserBalanceRepository userBalanceRepository) {
        this.transactionRepository = transactionRepository;
        this.userBalanceRepository = userBalanceRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> listTransactions(User currentUser,
            Category category,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository
                .findByFilter(currentUser.getId(), category, startDate, endDate, pageable)
                .map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public com.moneycontas.dto.response.TransactionSummaryResponse getSummary(User currentUser,
            java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.math.BigDecimal monthIncome = transactionRepository.sumAmountByTypeAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.INCOME, startDate, endDate);
        java.math.BigDecimal monthExpense = transactionRepository.sumAmountByTypeAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.EXPENSE, startDate, endDate);
        java.math.BigDecimal monthCredit = transactionRepository.sumAmountByPaymentMethodAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.PaymentMethod.CREDIT, startDate, endDate);
        java.math.BigDecimal monthDebit = transactionRepository.sumAmountByPaymentMethodAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.PaymentMethod.DEBIT, startDate, endDate);
        java.math.BigDecimal currentBalance = monthIncome.subtract(monthExpense);

        return new com.moneycontas.dto.response.TransactionSummaryResponse(
                currentBalance,
                monthIncome,
                monthExpense,
                monthCredit,
                monthDebit);
    }

    @Transactional
    public TransactionResponse create(User currentUser, TransactionRequest request) {
        int count = 1;
        if (request.installmentsCount() != null && request.installmentsCount() > 1) {
            count = request.installmentsCount();
        } else if (request.isRecurrent()) {
            count = request.frequency() == com.moneycontas.domain.enums.Frequency.MONTHLY ? 24 : 5;
        }

        Transaction firstTx = null;

        for (int i = 0; i < count; i++) {
            Transaction transaction = new Transaction();
            mapRequestToEntity(request, transaction);
            transaction.setUser(currentUser);

            if (request.installmentsCount() != null && request.installmentsCount() > 1) {
                transaction.setInstallmentNumber(i + 1);
                transaction.setInstallmentsCount(request.installmentsCount());
            }

            
            if (i > 0) {
                if (request.installmentsCount() != null && request.installmentsCount() > 1) {
                    transaction.setTransactionDate(request.transactionDate().plusMonths(i));
                } else if (request.isRecurrent()) {
                    if (request.frequency() == com.moneycontas.domain.enums.Frequency.MONTHLY) {
                        transaction.setTransactionDate(request.transactionDate().plusMonths(i));
                    } else if (request.frequency() == com.moneycontas.domain.enums.Frequency.ANNUAL) {
                        transaction.setTransactionDate(request.transactionDate().plusYears(i));
                    }
                }
            }

            transaction = transactionRepository.save(transaction);
            if (i == 0) {
                firstTx = transaction;
            }
        }

        refreshUserBalance(currentUser.getId());

        return TransactionResponse.from(firstTx);
    }

    @Transactional
    public TransactionResponse update(User currentUser, UUID id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        mapRequestToEntity(request, transaction);
        Transaction saved = transactionRepository.save(transaction);
        refreshUserBalance(currentUser.getId());
        return TransactionResponse.from(saved);
    }

    @Transactional
    public void delete(User currentUser, UUID id) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
        refreshUserBalance(currentUser.getId());
    }

    @Transactional
    public TransactionResponse carryOverToNextMonth(User currentUser, java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Invalid month range");
        }

        BigDecimal monthIncome = transactionRepository.sumAmountByTypeAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.INCOME, startDate, endDate);
        BigDecimal monthExpense = transactionRepository.sumAmountByTypeAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.EXPENSE, startDate, endDate);
        BigDecimal remaining = monthIncome.subtract(monthExpense);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No positive balance to carry over for selected month");
        }

        java.time.LocalDate nextMonthDate = endDate.plusDays(1).withDayOfMonth(1);
        String monthRef = startDate.getYear() + "-"
                + String.format("%02d", startDate.getMonthValue());
        String sourceDescription = "Saldo transferido " + monthRef;
        String targetDescription = "Saldo transportado " + monthRef;

        boolean alreadyCreatedTarget = transactionRepository.existsByUserIdAndTypeAndCategoryAndTransactionDateAndDescription(
                currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.INCOME,
                com.moneycontas.domain.enums.Category.RECEITA,
                nextMonthDate,
                targetDescription);

        boolean alreadyCreatedSource = transactionRepository.existsByUserIdAndTypeAndCategoryAndTransactionDateAndDescription(
                currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.EXPENSE,
                com.moneycontas.domain.enums.Category.RECEITA,
                endDate,
                sourceDescription);

        if (alreadyCreatedTarget || alreadyCreatedSource) {
            throw new IllegalStateException("Carry over already created for this month");
        }

        BigDecimal amount = remaining.setScale(2, java.math.RoundingMode.HALF_UP);

        Transaction outgoing = new Transaction();
        outgoing.setUser(currentUser);
        outgoing.setDescription(sourceDescription);
        outgoing.setAmount(amount);
        outgoing.setCategory(com.moneycontas.domain.enums.Category.RECEITA);
        outgoing.setTransactionDate(endDate);
        outgoing.setIsRecurrent(false);
        outgoing.setFrequency(null);
        outgoing.setInstallmentsCount(null);
        outgoing.setInstallmentNumber(null);
        outgoing.setType(com.moneycontas.domain.enums.TransactionType.EXPENSE);
        outgoing.setPaymentMethod(com.moneycontas.domain.enums.PaymentMethod.TRANSFER);
        transactionRepository.save(outgoing);

        Transaction incoming = new Transaction();
        incoming.setUser(currentUser);
        incoming.setDescription(targetDescription);
        incoming.setAmount(amount);
        incoming.setCategory(com.moneycontas.domain.enums.Category.RECEITA);
        incoming.setTransactionDate(nextMonthDate);
        incoming.setIsRecurrent(false);
        incoming.setFrequency(null);
        incoming.setInstallmentsCount(null);
        incoming.setInstallmentNumber(null);
        incoming.setType(com.moneycontas.domain.enums.TransactionType.INCOME);
        incoming.setPaymentMethod(com.moneycontas.domain.enums.PaymentMethod.TRANSFER);
        Transaction saved = transactionRepository.save(incoming);

        refreshUserBalance(currentUser.getId());
        return TransactionResponse.from(saved);
    }

    private void refreshUserBalance(UUID userId) {
        userBalanceRepository.findById(userId).ifPresent(balance -> {
            java.math.BigDecimal recalculated = transactionRepository.calculateCurrentBalance(userId);
            balance.setBalance(recalculated);
            userBalanceRepository.save(balance);
        });
    }

    private Transaction mapRequestToEntity(TransactionRequest request, Transaction transaction) {
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount().setScale(2, java.math.RoundingMode.HALF_UP));
        transaction.setCategory(request.category());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setIsRecurrent(request.isRecurrent());
        transaction.setFrequency(request.frequency());
        transaction.setInstallmentsCount(request.installmentsCount());
        transaction.setType(
                request.type() != null ? request.type() : com.moneycontas.domain.enums.TransactionType.EXPENSE);
        transaction.setPaymentMethod(request.paymentMethod());
        return transaction;
    }
}
