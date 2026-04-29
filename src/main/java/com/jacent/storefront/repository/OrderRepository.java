package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Order;
import com.jacent.storefront.entity.OrderItem;
import com.jacent.storefront.exception.ResourceNotFoundException;
import com.jacent.storefront.query.OrderQueries;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final OrderQueries orderQueries;

    public OrderRepository(JdbcTemplate jdbcTemplate, OrderQueries orderQueries) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderQueries = orderQueries;
    }

    public int insertOrder(int userId, String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(orderQueries.getCreateOrder(), Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setString(2, status);
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() == null) {
                throw new RuntimeException("Failed to retrieve generated order ID");
            }

            return keyHolder.getKey().intValue();

        } catch (DataAccessException ex) {
            throw new RuntimeException("Error inserting order", ex);
        }
    }

    public void insertOrderItem(int orderId, OrderItem item) {
        try {
            jdbcTemplate.update(orderQueries.getAddItemToOrder(),
                    orderId,
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getRetailPrice()
            );
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error inserting order item for orderId: " + orderId, ex);
        }
    }

    public List<Order> findOrdersByUser(int userId) {
        try {
            return jdbcTemplate.query(orderQueries.getOrdersByUserId(),
                    new Object[]{userId},
                    new BeanPropertyRowMapper<>(Order.class));
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error fetching orders for userId: " + userId, ex);
        }
    }

    public Order findOrderById(int orderId) {
        try {
            return jdbcTemplate.queryForObject(orderQueries.getOrderByOrderId(),
                    new Object[]{orderId},
                    new BeanPropertyRowMapper<>(Order.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error fetching order with id: " + orderId, ex);
        }
    }

    public List<OrderItem> findItemsByOrderId(int orderId) {
        try {
            return jdbcTemplate.query(orderQueries.getOrderItemsByOrderId(),
                    new Object[]{orderId},
                    new BeanPropertyRowMapper<>(OrderItem.class));
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error fetching items for orderId: " + orderId, ex);
        }
    }
}
