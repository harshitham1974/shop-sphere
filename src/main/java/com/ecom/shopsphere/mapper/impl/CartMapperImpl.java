package com.ecom.shopsphere.mapper.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ecom.shopsphere.dto.response.CartItemResponseDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.entity.Cart;
import com.ecom.shopsphere.entity.CartItem;
import com.ecom.shopsphere.mapper.CartMapper;

@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public CartItemResponseDTO toCartItemResponseDTO(CartItem cartItem) {

        BigDecimal totalPrice = cartItem.getPriceAtAddedTime()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponseDTO.builder()
                .cartItemId(cartItem.getCartItemId())
                .productId(cartItem.getProduct().getProductId())
                .productName(cartItem.getProduct().getProductName())
                .imageUrl(cartItem.getProduct().getImageUrl())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getPriceAtAddedTime())
                .totalPrice(totalPrice)
                .build();
    }

    @Override
    public CartResponseDTO toCartResponseDTO(Cart cart) {

        List<CartItemResponseDTO> items = cart.getCartItems()
                .stream()
                .map(this::toCartItemResponseDTO)
                .toList();

        Integer totalItems = cart.getCartItems()
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalAmount = cart.getCartItems()
                .stream()
                .map(item -> item.getPriceAtAddedTime()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .items(items)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }
}