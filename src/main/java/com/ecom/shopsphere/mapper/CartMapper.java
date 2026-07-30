package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.response.CartItemResponseDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.entity.Cart;
import com.ecom.shopsphere.entity.CartItem;

public interface CartMapper {

    CartItemResponseDTO toCartItemResponseDTO(CartItem cartItem);

    CartResponseDTO toCartResponseDTO(Cart cart);

}