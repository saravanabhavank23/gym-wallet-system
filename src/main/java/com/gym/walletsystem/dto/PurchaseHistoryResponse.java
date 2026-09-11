package com.gym.walletsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseHistoryResponse {
    private String productName;
    private BigDecimal priceAtPurchase;
    private LocalDateTime purchasedAt;
}