package com.jacent.storefront.service;

import com.jacent.storefront.dto.response.OrderDetailsResponse;
import com.jacent.storefront.entity.Order;

import java.util.List;

public interface OrderService {
    int createOrder();

    List<Order> getCurrentUserOrders();

    OrderDetailsResponse getOrderDetails(int orderId);

    int reorder(int orderId);
}
