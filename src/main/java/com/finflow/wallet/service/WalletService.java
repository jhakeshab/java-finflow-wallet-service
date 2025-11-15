package com.finflow.wallet.service;

import com.finflow.wallet.entity.Wallet;
import com.finflow.wallet.entity.WalletTransaction;
import com.finflow.wallet.repository.WalletRepository;
import com.finflow.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;

    private final String AUTH_URL = "http://localhost:9001/api/auth";

    public Wallet createWallet(Long userId, String currency) {
        // KYC check via Auth
        Map user = restTemplate.getForObject(AUTH_URL + "/user/" + userId, Map.class);
        if (user == null || !"verified".equals(user.get("kycStatus"))) {
            throw new RuntimeException("KYC not verified");
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet saved = walletRepository.save(wallet);

        // Cache balance
        redisTemplate.opsForValue().set("balance:" + saved.getId(), BigDecimal.ZERO);
        // Publish event
        kafkaTemplate.send("wallet.created", "Wallet ID: " + saved.getId());

        return saved;
    }

    public BigDecimal getBalance(Long walletId) {
        Object cached = redisTemplate.opsForValue().get("balance:" + walletId);
        if (cached != null) {
            return new BigDecimal(cached.toString());
        }
        Wallet wallet = walletRepository.findById(walletId).orElseThrow();
        redisTemplate.opsForValue().set("balance:" + walletId, wallet.getBalance());
        return wallet.getBalance();
    }

    public List<WalletTransaction> getTransactions(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }

    public void updateBalance(Long walletId, BigDecimal amount, String type, String referenceId, String description) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow();
        BigDecimal newBalance = "credit".equals(type) ? wallet.getBalance().add(amount) : wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Update cache
        redisTemplate.opsForValue().set("balance:" + walletId, newBalance);

        // Create transaction
        WalletTransaction tx = new WalletTransaction();
        tx.setWalletId(walletId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(newBalance);
        tx.setReferenceId(referenceId);
        tx.setDescription(description);
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        // Publish event
        kafkaTemplate.send("wallet.balance_updated", "Wallet ID: " + walletId + ", Amount: " + amount);
    }

    public void updateStatus(Long walletId, String status) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow();
        wallet.setStatus(status);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        kafkaTemplate.send("wallet.status_changed", "Wallet ID: " + walletId + ", Status: " + status);
    }
}