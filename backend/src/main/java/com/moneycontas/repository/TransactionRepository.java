package com.moneycontas.repository;

import com.moneycontas.domain.entity.Transaction;
import com.moneycontas.domain.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

        
        @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
                        "AND (:category IS NULL OR t.category = :category) " +
                        "AND (cast(:startDate as date) IS NULL OR t.transactionDate >= :startDate) " +
                        "AND (cast(:endDate as date) IS NULL OR t.transactionDate <= :endDate) " +
                        "ORDER BY t.transactionDate DESC")
        Page<Transaction> findByFilter(
                        @Param("userId") UUID userId,
                        @Param("category") Category category,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate,
                        Pageable pageable);

        
        List<Transaction> findByUserIdAndIsRecurrentTrue(UUID userId);

        
        Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

        
        long countByUserId(UUID userId);

        
        @Query("SELECT t.category AS category, SUM(t.amount) AS total " +
                        "FROM Transaction t WHERE t.user.id = :userId " +
                        "AND t.type = 'EXPENSE' " +
                        "GROUP BY t.category")
        List<CategoryTotal> sumAmountByCategoryForUser(@Param("userId") UUID userId);

        @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
                        "FROM Transaction t WHERE t.user.id = :userId " +
                        "AND t.transactionDate <= CURRENT_DATE")
        BigDecimal calculateCurrentBalance(@Param("userId") UUID userId);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId " +
                        "AND t.type = :type AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
        BigDecimal sumAmountByTypeAndDate(@Param("userId") UUID userId,
                        @Param("type") com.moneycontas.domain.enums.TransactionType type,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId " +
                        "AND t.paymentMethod = :method AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
        BigDecimal sumAmountByPaymentMethodAndDate(@Param("userId") UUID userId,
                        @Param("method") com.moneycontas.domain.enums.PaymentMethod method,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);

        
        @Query("SELECT FUNCTION('to_char', t.transactionDate, 'YYYY-MM') AS month, SUM(t.amount) AS total " +
                        "FROM Transaction t WHERE t.user.id = :userId " +
                        "AND (:year IS NULL OR FUNCTION('year', t.transactionDate) = :year) " +
                        "GROUP BY FUNCTION('to_char', t.transactionDate, 'YYYY-MM') " +
                        "ORDER BY FUNCTION('to_char', t.transactionDate, 'YYYY-MM') ASC")
        List<MonthlyTotal> sumAmountByMonthForUser(@Param("userId") UUID userId, @Param("year") Integer year);

        
        interface CategoryTotal {
                Category getCategory();

                BigDecimal getTotal();
        }

        
        interface MonthlyTotal {
                String getMonth();

                BigDecimal getTotal();
        }
}
