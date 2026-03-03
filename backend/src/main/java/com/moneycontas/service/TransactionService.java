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
        java.math.BigDecimal currentBalance = userBalanceRepository.findById(currentUser.getId())
                .map(com.moneycontas.domain.entity.UserBalance::getBalance)
                .orElse(java.math.BigDecimal.ZERO);
        java.math.BigDecimal monthIncome = transactionRepository.sumAmountByTypeAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.INCOME, startDate, endDate);
        java.math.BigDecimal monthExpense = transactionRepository.sumAmountByTypeAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.TransactionType.EXPENSE, startDate, endDate);
        java.math.BigDecimal monthCredit = transactionRepository.sumAmountByPaymentMethodAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.PaymentMethod.CREDIT, startDate, endDate);
        java.math.BigDecimal monthDebit = transactionRepository.sumAmountByPaymentMethodAndDate(currentUser.getId(),
                com.moneycontas.domain.enums.PaymentMethod.DEBIT, startDate, endDate);

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

        com.moneycontas.domain.entity.UserBalance userBalance = userBalanceRepository.findById(currentUser.getId())
                .orElse(null);
        if (userBalance != null) {
            java.math.BigDecimal totalAmountAdded = firstTx.getAmount().multiply(new java.math.BigDecimal(count));
            if (firstTx.getType() == com.moneycontas.domain.enums.TransactionType.INCOME) {
                userBalance.setBalance(userBalance.getBalance().add(totalAmountAdded));
            } else {
                userBalance.setBalance(userBalance.getBalance().subtract(totalAmountAdded));
            }
            userBalanceRepository.save(userBalance);
        }

        return TransactionResponse.from(firstTx);
    }

    @Transactional
    public TransactionResponse update(User currentUser, UUID id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        java.math.BigDecimal oldAmount = transaction.getAmount();
        com.moneycontas.domain.enums.TransactionType oldType = transaction.getType();

        mapRequestToEntity(request, transaction);

        com.moneycontas.domain.entity.UserBalance userBalance = userBalanceRepository.findById(currentUser.getId())
                .orElse(null);
        if (userBalance != null) {
            
            if (oldType == com.moneycontas.domain.enums.TransactionType.INCOME) {
                userBalance.setBalance(userBalance.getBalance().subtract(oldAmount));
            } else {
                userBalance.setBalance(userBalance.getBalance().add(oldAmount));
            }
            
            if (transaction.getType() == com.moneycontas.domain.enums.TransactionType.INCOME) {
                userBalance.setBalance(userBalance.getBalance().add(transaction.getAmount()));
            } else {
                userBalance.setBalance(userBalance.getBalance().subtract(transaction.getAmount()));
            }
            userBalanceRepository.save(userBalance);
        }

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(User currentUser, UUID id) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        com.moneycontas.domain.entity.UserBalance userBalance = userBalanceRepository.findById(currentUser.getId())
                .orElse(null);
        if (userBalance != null) {
            if (transaction.getType() == com.moneycontas.domain.enums.TransactionType.INCOME) {
                userBalance.setBalance(userBalance.getBalance().subtract(transaction.getAmount()));
            } else {
                userBalance.setBalance(userBalance.getBalance().add(transaction.getAmount()));
            }
            userBalanceRepository.save(userBalance);
        }

        transactionRepository.delete(transaction);
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
