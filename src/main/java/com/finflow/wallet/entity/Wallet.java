package com.finflow.wallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String currency;
    private BigDecimal balance = BigDecimal.ZERO;
    private String status = "active"; // active/frozen/closed

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}