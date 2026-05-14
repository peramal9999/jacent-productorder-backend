package com.jacent.storefront.service;

import com.jacent.storefront.dto.response.OrderDetailsResponse;
import com.jacent.storefront.entity.Order;

import java.util.List;

public interface OrderService {
    String createOrder();

    List<Order> getCurrentUserOrders();

    OrderDetailsResponse getOrderDetails(String orderId);

    String reorder(String orderId);
}
