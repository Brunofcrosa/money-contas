package com.moneycontas.controller;

import com.moneycontas.domain.entity.User;
import com.moneycontas.domain.enums.Category;
import com.moneycontas.dto.request.TransactionRequest;
import com.moneycontas.dto.response.TransactionResponse;
import com.moneycontas.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TransactionResponse> result = transactionService.listTransactions(currentUser, category, startDate,
                endDate, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<com.moneycontas.dto.response.TransactionSummaryResponse> getSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {

        return ResponseEntity.ok(transactionService.getSummary(currentUser, startDate, endDate));
    }

    
    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse response = transactionService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse response = transactionService.update(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        transactionService.delete(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
