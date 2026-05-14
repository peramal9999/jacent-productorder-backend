package com.jacent.storefront.controller;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.dto.response.CartItemResponse;
import com.jacent.storefront.dto.response.CartResponse;
import com.jacent.storefront.exception.AccessDeniedException;
import com.jacent.storefront.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/cart")
@RestController
public class CartController {

    private final CartService cartService;
    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCartByUser());
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItemToCart(@Valid @RequestBody CartItemRequest cartItemRequest) throws AccessDeniedException {
        CartItemResponse cartItemResponse = cartService.addItemToCart(cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemResponse);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateItem(@PathVariable String cartItemId,
            @Valid @RequestBody CartItemRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(cartService.updateItem(cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Boolean> removeItem(@PathVariable String cartItemId) throws AccessDeniedException {
        return ResponseEntity.ok(cartService.removeItem(cartItemId));
    }

    @DeleteMapping
    public ResponseEntity<Boolean> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(true);
    }
}
