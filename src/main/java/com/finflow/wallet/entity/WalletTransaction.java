package com.finflow.wallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long walletId;
    private String type; // credit/debit
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceId;
    private String description;
    private LocalDateTime timestamp;
}