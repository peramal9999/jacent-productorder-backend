package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Order;
import com.jacent.storefront.entity.OrderItem;
import com.jacent.storefront.exception.ResourceNotFoundException;
import com.jacent.storefront.query.OrderQueries;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderRepository {

    private final  NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final OrderQueries orderQueries;

    public OrderRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, OrderQueries orderQueries) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.orderQueries = orderQueries;
    }

    public int insertOrder(int userId, String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("status", status);
        params.addValue("orderDate", Timestamp.valueOf(LocalDateTime.now()));

        namedParameterJdbcTemplate.update(
                orderQueries.getCreateOrder(),
                params,
                keyHolder
        );

        if (keyHolder.getKey() == null) {
            throw new RuntimeException("Failed to retrieve generated order ID");
        }

        return keyHolder.getKey().intValue();
    }

    public void insertOrderItem(int orderId, OrderItem item) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("orderId", orderId);
            params.addValue("itemId", item.getItemId());
            params.addValue("quantity", item.getQuantity());
            params.addValue("unitPrice", item.getUnitPrice());
            params.addValue("retailPrice", item.getRetailPrice());
            namedParameterJdbcTemplate.update(
                    orderQueries.getAddItemToOrder(),
                    params
            );
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error inserting order item for orderId: " + orderId, ex);
        }
    }

    public List<Order> findOrdersByUser(int userId, LocalDateTime threeMonthsAgo) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("userId", userId);
            params.addValue("startDate", Timestamp.valueOf(threeMonthsAgo));

            return namedParameterJdbcTemplate.query(
                    orderQueries.getOrdersByUserId(),
                    params,
                    new BeanPropertyRowMapper<>(Order.class)
            );
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error fetching orders for userId: " + userId, ex);
        }
    }

    public Order findOrderById(int orderId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("orderId", orderId);

            return namedParameterJdbcTemplate.queryForObject(
                    orderQueries.getOrderByOrderId(),
                    params,
                    new BeanPropertyRowMapper<>(Order.class)
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error fetching order with id: " + orderId, ex);
        }
    }

    public List<OrderItem> findItemsByOrderId(int orderId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("orderId", orderId);

            return namedParameterJdbcTemplate.query(
                    orderQueries.getOrderItemsByOrderId(),
                    params,
                    new BeanPropertyRowMapper<>(OrderItem.class)
            );
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error fetching items for orderId: " + orderId, ex);
        }
    }
}
