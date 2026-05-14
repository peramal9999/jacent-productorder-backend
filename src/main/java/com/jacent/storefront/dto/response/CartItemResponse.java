package com.jacent.storefront.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CartItemResponse {
    private String cartItemId;
    private int itemId;
    private String itemName;
    private String itemDesc;
    private int quantity;
    private BigDecimal price;
    private BigDecimal retailPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}