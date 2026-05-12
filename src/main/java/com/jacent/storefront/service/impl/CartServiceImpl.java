package com.jacent.storefront.service.impl;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.dto.response.CartItemResponse;
import com.jacent.storefront.dto.response.CartResponse;
import com.jacent.storefront.entity.Item;
import com.jacent.storefront.exception.AccessDeniedException;
import com.jacent.storefront.exception.ResourceNotFoundException;
import com.jacent.storefront.entity.Cart;
import com.jacent.storefront.entity.CartItem;
import com.jacent.storefront.entity.User;
import com.jacent.storefront.repository.CartRepository;
import com.jacent.storefront.repository.ItemRepository;
import com.jacent.storefront.service.CartService;
import com.jacent.storefront.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public CartServiceImpl(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public CartResponse getCartByUser() {
        User user = SecurityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(user.getUserId());
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getCartId());
        return toCartResponse(cart, items);
    }

    @Transactional
    @Override
    public CartResponse addItemToCart(CartItemRequest cartItemRequest) throws AccessDeniedException {
        User user = SecurityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(user.getUserId());
        Item item = itemRepository.getItemById(cartItemRequest.getItemId());
        if(!item.getStoreId().equals(user.getStoreId())){
            throw new AccessDeniedException("You cannot add items from a different store to your cart");
        }
        Optional<CartItem> existing = cartRepository
                .findItemByCartIdAndItemId(cart.getCartId(), cartItemRequest.getItemId());

        if (existing.isPresent()) {
            cartRepository.updateItemQuantity(existing.get().getCartItemId(), cartItemRequest.getQuantity());
        } else {
            cartRepository.addItemToCart(cart.getCartId(), cartItemRequest);
        }

        List<CartItem> items = cartRepository.findItemsByCartId(cart.getCartId());
        return toCartResponse(cart, items);
    }


    // Update quantity
    @Override
    public CartResponse updateItem(int cartItemId, CartItemRequest request) throws AccessDeniedException {
        User user = SecurityUtils.getCurrentUser();
        // ✓ verify item belongs to user's cart
        verifyCartItemOwnership(user.getUserId(), cartItemId);

        Cart cart = getOrCreateCart(user.getUserId());
        cartRepository.updateItemQuantity(cartItemId, request.getQuantity());
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getCartId());
        return toCartResponse(cart, items);
    }

    // Remove single item
    @Override
    public CartResponse removeItem(int cartItemId) throws AccessDeniedException {
        User user = SecurityUtils.getCurrentUser();
        // ✓ verify item belongs to user's cart
        verifyCartItemOwnership(user.getUserId(), cartItemId);

        Cart cart = getOrCreateCart(user.getUserId());
        cartRepository.removeItem(cartItemId);
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getCartId());
        return toCartResponse(cart, items);
    }

    // Clear all items
    @Transactional
    @Override
    public void clearCart() {
        User user = SecurityUtils.getCurrentUser();
        cartRepository.findCartByUserId(user.getUserId())
                .ifPresent(cart -> cartRepository.clearCart(cart.getCartId()));
    }

    // Get or create active cart
    private Cart getOrCreateCart(int userId) {
        return cartRepository.findCartByUserId(userId)
                .orElseGet(() -> cartRepository.createCart(userId));
    }

    public Cart verifyCartOwnership(int userId, int cartId) throws AccessDeniedException {
        Cart cart = cartRepository.findCartById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));

        if (cart.getUserId() != userId) {
            log.warn("Unauthorized cart access: userId={} tried to access cartId={}", userId, cartId);
            throw new AccessDeniedException("You do not have access to this cart");
        }
        return cart;
    }

    public CartItem verifyCartItemOwnership(int userId, int cartItemId) throws AccessDeniedException {
        CartItem item = cartRepository.findItemById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        Cart cart = cartRepository.findCartById(item.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: "+ item.getCartId()));

        if (cart.getUserId() != userId) {
            log.warn("Unauthorized cart item access: userId={} tried to access cartItemId={}", userId, cartItemId);
            throw new AccessDeniedException("You do not have access to this cart item");
        }
        return item;
    }

    private CartItemResponse toItemResponse(CartItem cartItem) {
        Item item = itemRepository.getItemById(cartItem.getItemId());
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .itemId(cartItem.getItemId())
                .itemName(item.getItemName())
                .quantity(cartItem.getQuantity())
                .price(item.getPrice())
                .retailPrice(item.getRetailPrice())
                .addedAt(cartItem.getAddedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }

    private CartResponse toCartResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

}
