package com.finflow.wallet.controller;

import com.finflow.wallet.entity.Wallet;
import com.finflow.wallet.entity.WalletTransaction;
import com.finflow.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<Wallet> create(@RequestBody Map<String, String> req, @RequestParam Long userId) {
        return ResponseEntity.ok(walletService.createWallet(userId, req.get("currency")));
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long walletId) {
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable Long walletId) {
        return ResponseEntity.ok(walletService.getTransactions(walletId));
    }

    @PostMapping("/{walletId}/update-balance")
    public ResponseEntity<String> updateBalance(@PathVariable Long walletId, @RequestBody Map<String, Object> req) {
        walletService.updateBalance(walletId, new BigDecimal(req.get("amount").toString()), (String) req.get("type"), (String) req.get("referenceId"), (String) req.get("description"));
        return ResponseEntity.ok("Balance updated");
    }

    @PutMapping("/{walletId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long walletId, @RequestParam String status) {
        walletService.updateStatus(walletId, status);
        return ResponseEntity.ok("Status updated");
    }
}