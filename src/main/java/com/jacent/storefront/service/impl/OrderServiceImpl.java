package com.jacent.storefront.service.impl;

import com.jacent.storefront.dto.response.CartItemResponse;
import com.jacent.storefront.dto.response.CartResponse;
import com.jacent.storefront.dto.response.OrderDetailsResponse;
import com.jacent.storefront.entity.Configuration;
import com.jacent.storefront.entity.Order;
import com.jacent.storefront.entity.OrderItem;
import com.jacent.storefront.entity.User;
import com.jacent.storefront.repository.OrderRepository;
import com.jacent.storefront.service.CartService;
import com.jacent.storefront.service.ConfigurationService;
import com.jacent.storefront.service.OrderService;
import com.jacent.storefront.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ConfigurationService configurationService;

    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService, ConfigurationService configurationService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.configurationService = configurationService;
    }

    @Transactional
    @Override
    public int createOrder() {
        User currentUser = SecurityUtils.getCurrentUser();
        int orderId = orderRepository.insertOrder(currentUser.getUserId(), "pending");
        CartResponse cart = cartService.getCartByUser();
        for (CartItemResponse item : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(orderId)
                    .itemId(item.getItemId())
                    .quantity(item.getQuantity())
                    .unitPrice(BigDecimal.ZERO)
                    .retailPrice(BigDecimal.ZERO)
                    .build();
            orderRepository.insertOrderItem(orderId, orderItem);
        }
        cartService.clearCart();
        return orderId;
    }

    @Override
    public List<Order> getCurrentUserOrders() {
        User currentUser = SecurityUtils.getCurrentUser();
        Integer pastOrdersUptoMonths = configurationService.getValueAsInteger(Configuration.DISPLAY_PAST_ORDERS_MAX_LIMIT, 3);

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(pastOrdersUptoMonths);
        return orderRepository.findOrdersByUser(currentUser.getUserId(), threeMonthsAgo);
    }

    @Override
    public OrderDetailsResponse getOrderDetails(int orderId) {
        Order order = orderRepository.findOrderById(orderId);
        List<OrderItem> items = orderRepository.findItemsByOrderId(orderId);

        OrderDetailsResponse response = new OrderDetailsResponse();
        response.setOrder(order);
        response.setOrderItem(items);
        return response;
    }

    @Override
    public int reorder(int oldOrderId) {
        Order oldOrder = orderRepository.findOrderById(oldOrderId);
        User currentUser = SecurityUtils.getCurrentUser();
        int newOrderId = orderRepository.insertOrder(currentUser.getUserId(), "pending");

        List<OrderItem> items = orderRepository.findItemsByOrderId(oldOrderId);

        for (OrderItem item : items) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(newOrderId)
                    .itemId(item.getItemId())
                    .quantity(item.getQuantity())
                    .unitPrice(BigDecimal.ZERO)
                    .retailPrice(BigDecimal.ZERO)
                    .build();

            orderRepository.insertOrderItem(newOrderId, orderItem);
        }

        return newOrderId;
    }
}
