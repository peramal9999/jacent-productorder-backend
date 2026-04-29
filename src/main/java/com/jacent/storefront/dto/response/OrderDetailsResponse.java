package com.jacent.storefront.dto.response;

import com.jacent.storefront.entity.Order;
import com.jacent.storefront.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailsResponse {
    private Order order;
    private List<OrderItem> orderItem;
}
