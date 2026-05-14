package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String orderItemId;
    private String orderId;
    private int itemId;
    private String itemDesc;
    private BigDecimal unitPrice;
    private BigDecimal retailPrice;
    private int quantity;
}
