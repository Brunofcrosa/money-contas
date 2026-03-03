package com.moneycontas.controller;

import com.moneycontas.domain.entity.User;
import com.moneycontas.dto.response.DashboardSummaryResponse;
import com.moneycontas.dto.response.TransactionResponse;
import com.moneycontas.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dashboardService.getSummary(currentUser));
    }

    
    @GetMapping("/recurrent")
    public ResponseEntity<List<TransactionResponse>> recurrent(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dashboardService.getRecurrentTransactions(currentUser));
    }
}
