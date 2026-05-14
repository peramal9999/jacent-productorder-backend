package com.jacent.storefront.service;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.dto.response.CartItemResponse;
import com.jacent.storefront.dto.response.CartResponse;
import com.jacent.storefront.exception.AccessDeniedException;
import jakarta.validation.Valid;


public interface CartService {
    CartResponse getCartByUser();

    CartItemResponse addItemToCart(@Valid CartItemRequest cartItemRequest) throws AccessDeniedException;

    CartItemResponse updateItem(String cartItemId, @Valid CartItemRequest request) throws AccessDeniedException;

    Boolean removeItem(String cartItemId) throws AccessDeniedException;

    void clearCart();


}
