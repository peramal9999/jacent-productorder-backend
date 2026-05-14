package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private String cartItemId;
    private String cartId;
    private int itemId;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
