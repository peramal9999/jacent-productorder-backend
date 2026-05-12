package com.jacent.storefront.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CartItemResponse {
    private int cartItemId;
    private int itemId;
    private String itemName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal retailPrice;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}