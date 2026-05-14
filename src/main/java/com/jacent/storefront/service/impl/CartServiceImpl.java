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
        List<CartItem> cartItems = cartRepository.findItemsByCartId(cart.getCartId());
        List<Item> items = List.of();
        if(cartItems.size() > 0){
            List<Integer> itemIds = cartItems.stream().map(cartItem -> cartItem.getItemId()).toList();
            items = itemRepository.getAllItemsByIdIn(user.getStoreId(), itemIds);
        }
        return toCartResponse(cart, cartItems, items);
    }

    @Transactional
    @Override
    public CartItemResponse addItemToCart(CartItemRequest cartItemRequest) throws AccessDeniedException {
        User user = SecurityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(user.getUserId());
        Item item = itemRepository.getItemById(user.getStoreId(), cartItemRequest.getItemId());
        if(!item.getStoreId().equals(user.getStoreId())){
            throw new AccessDeniedException("You cannot add items from a different store to your cart");
        }
        Optional<CartItem> existing = cartRepository
                .findItemByCartIdAndItemId(cart.getCartId(), cartItemRequest.getItemId());

        CartItem cartItem;
        if (existing.isPresent()) {
            cartItem = cartRepository.updateItemQuantity(existing.get().getCartItemId(), cartItemRequest.getQuantity());
        } else {
            cartItem = cartRepository.addItemToCart(cart.getCartId(), cartItemRequest);
        }
        return toCartItemResponse(item, cartItem);
    }


    // Update quantity
    @Override
    public CartItemResponse updateItem(String cartItemId, CartItemRequest request) throws AccessDeniedException {
        User user = SecurityUtils.getCurrentUser();
        // ✓ verify item belongs to user's cart
        CartItem cartItem = cartRepository.findItemById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        Cart cart = cartRepository.findCartById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: "+ cartItem.getCartId()));

        verifyCartItemOwnership(user.getUserId(), cart);
        Item item = itemRepository.getItemById(user.getStoreId(), request.getItemId());
        CartItem updatedCartItem = cartRepository.updateItemQuantity(cartItemId, request.getQuantity());
        return toCartItemResponse(item, updatedCartItem);
    }

    // Remove single item
    @Override
    public Boolean removeItem(String cartItemId) throws AccessDeniedException {
        User user = SecurityUtils.getCurrentUser();

        CartItem cartItem = cartRepository.findItemById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        Cart cart = cartRepository.findCartById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: "+ cartItem.getCartId()));

        // ✓ verify item belongs to user's cart
        verifyCartItemOwnership(user.getUserId(), cart);

        cartRepository.removeItem(cartItemId);
        return true;
    }

    // Clear all items
    @Transactional
    @Override
    public void clearCart() {
        User user = SecurityUtils.getCurrentUser();
        cartRepository.findCartByUserId(user.getUserId())
                .ifPresent(cart -> {
                    // ✓ verify item belongs to user's cart
                    verifyCartItemOwnership(user.getUserId(), cart);
                    cartRepository.clearCart(cart.getCartId());
                });
    }

    // Get or create active cart
    private Cart getOrCreateCart(String userId) {
        return cartRepository.findCartByUserId(userId)
                .orElseGet(() -> cartRepository.createCart(userId));
    }

    public Cart verifyCartOwnership(String userId, String cartId) throws AccessDeniedException {
        Cart cart = cartRepository.findCartById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));

        if (!userId.equals(cart.getUserId())) {
            log.warn("Unauthorized cart access: userId={} tried to access cartId={}", userId, cartId);
            throw new AccessDeniedException("You do not have access to this cart");
        }
        return cart;
    }

    private void verifyCartItemOwnership(String userId, Cart cart) throws AccessDeniedException {
        if (!userId.equals(cart.getUserId())) {
            log.warn("Unauthorized cart item access: userId={} tried to access cartId={}", userId, cart.getCartId());
            throw new AccessDeniedException("You do not have access to this cart item");
        }
    }

    private CartItemResponse toCartItemResponse(Item item, CartItem cartItem) {
        User user = SecurityUtils.getCurrentUser();
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .itemId(cartItem.getItemId())
                .itemName(item.getItemName())
                .itemDesc(item.getItemDesc())
                .quantity(cartItem.getQuantity())
                .price(item.getPrice())
                .retailPrice(item.getRetailPrice())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }

    private CartResponse toCartResponse(Cart cart, List<CartItem> cartItems, List<Item> items) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(cartItem -> {
                    Item item = items.stream()
                            .filter(i -> i.getItemId() == cartItem.getItemId())
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Item not found for cart item: " + cartItem.getCartItemId()));
                    return toCartItemResponse(item, cartItem);
                })
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
