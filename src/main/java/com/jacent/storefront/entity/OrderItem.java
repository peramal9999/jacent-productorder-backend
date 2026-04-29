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
    private int orderItemId;
    private int orderId;
    private int productId;
    private BigDecimal unitPrice;
    private BigDecimal retailPrice;
    private int quantity;
}
