package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCartResponseDTO;

public interface CartService {

    CartResponseDTO addToCart( AddCartItemRequestDTO request);

    CartResponseDTO getCart();

    CartResponseDTO updateCartItem(
            Long cartItemId,
            UpdateCartItemRequestDTO request);

    DeleteCartResponseDTO removeCartItem(
            Long cartItemId);

    DeleteCartResponseDTO clearCart();

}