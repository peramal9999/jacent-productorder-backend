package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private int orderId;
    private int userId;
    private String netsuiteOrderId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
