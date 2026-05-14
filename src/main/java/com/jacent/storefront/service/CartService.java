package com.jacent.storefront.service;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.dto.response.CartResponse;
import com.jacent.storefront.exception.AccessDeniedException;
import jakarta.validation.Valid;


public interface CartService {
    CartResponse getCartByUser();

    CartResponse addItemToCart(@Valid CartItemRequest cartItemRequest) throws AccessDeniedException;

    CartResponse updateItem(String cartItemId, @Valid CartItemRequest request) throws AccessDeniedException;

    CartResponse removeItem(String cartItemId) throws AccessDeniedException;

    void clearCart();


}
