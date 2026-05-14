package com.jacent.storefront.repository;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.entity.Cart;
import com.jacent.storefront.entity.CartItem;
import com.jacent.storefront.query.CartQueries;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    CartQueries cartQueries;

    public Optional<Cart> findCartByUserId(String userId) {
        try {
            Cart cart = jdbcTemplate.queryForObject(
                    cartQueries.getCartByUserId(),
                    new BeanPropertyRowMapper<>(Cart.class),
                    userId
            );
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Cart createCart(String userId) {
        String cartId = UUID.randomUUID().toString();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(cartQueries.getCreateCart());
            ps.setString(1, cartId);
            ps.setString(2, userId);
            return ps;
        });

        return findCartById(cartId)
                .orElseThrow(() -> new RuntimeException("Failed to create cart for user: " + userId));
    }

    public Optional<Cart> findCartById(String cartId) {
        try {
            Cart cart = jdbcTemplate.queryForObject(
                    cartQueries.getCartByCartId(),
                    new SnowflakeBeanPropertyRowMapper<>(Cart.class),
                    cartId
            );
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<CartItem> findItemsByCartId(String cartId) {
        return jdbcTemplate.query(
                cartQueries.getCartItemsByCartId(),
                new SnowflakeBeanPropertyRowMapper<>(CartItem.class),
                cartId
        );
    }

    public Optional<CartItem> findItemByCartIdAndItemId(String cartId, int itemId) {
        try {
            CartItem item = jdbcTemplate.queryForObject(
                    cartQueries.getCartItemByCartIdAndItemId(),
                    new BeanPropertyRowMapper<>(CartItem.class),
                    cartId, itemId
            );
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public CartItem addItemToCart(String cartId, CartItemRequest request) {
        String cartItemId = UUID.randomUUID().toString();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(cartQueries.getAddItemToCart());
            ps.setString(1, cartItemId);
            ps.setString(2, cartId);
            ps.setInt(3, request.getItemId());
            ps.setInt(4, request.getQuantity());
            return ps;
        });

        return findItemById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Failed to add cart item"));
    }

    public CartItem updateItemQuantity(String cartItemId, int quantity) {
        jdbcTemplate.update(
                cartQueries.getUpdateCartItemQuantity(),
                quantity, cartItemId
        );
        return findItemById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));
    }

    public void removeItem(String cartItemId) {
        jdbcTemplate.update(
                cartQueries.getDeleteCartItemByCartItemId(),
                cartItemId
        );
    }

    public void clearCart(String cartId) {
        jdbcTemplate.update(
                cartQueries.getDeleteCart(),
                cartId
        );
    }

    public Optional<CartItem> findItemById(String cartItemId) {
        try {
            CartItem item = jdbcTemplate.queryForObject(
                    cartQueries.getCartItemByCartItemId(),
                    new BeanPropertyRowMapper<>(CartItem.class),
                    cartItemId
            );
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
