package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.response.DeleteAccountResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCartResponseDTO;
import com.ecom.shopsphere.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.entity.Cart;
import com.ecom.shopsphere.entity.CartItem;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.CartItemNotFoundException;
import com.ecom.shopsphere.exception.CartNotFoundException;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.exception.UserNotFoundException;
import com.ecom.shopsphere.mapper.CartMapper;
import com.ecom.shopsphere.repository.CartItemRepository;
import com.ecom.shopsphere.repository.CartRepository;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.service.CartService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final CartMapper cartMapper;

    private final CurrentUserService currentUserService;

    @Override
    public CartResponseDTO addToCart(AddCartItemRequestDTO request) {

        User user = currentUserService.getCurrentUser();

        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found."));

        Product product = productRepository.findById(
                        request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found."));

        CartItem cartItem =
                cartItemRepository
                        .findByCartCartIdAndProductProductId(
                                cart.getCartId(),
                                product.getProductId())
                        .orElse(null);

        if (cartItem != null) {

            cartItem.setQuantity(
                    cartItem.getQuantity()
                            + request.getQuantity());

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtAddedTime(product.getPrice())
                    .build();

        }

        cartItemRepository.save(cartItem);

        log.info(
                "Product {} added to cart of user {}.",
                product.getProductId());

        return cartMapper.toCartResponseDTO(cart);

    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart() {

        User user = currentUserService.getCurrentUser();

        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found."));

        return cartMapper.toCartResponseDTO(cart);
    }

    @Override
    public CartResponseDTO updateCartItem(
            Long cartItemId,
            UpdateCartItemRequestDTO request) {

        User user = currentUserService.getCurrentUser();

        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Cart item not found."));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {

            throw new CartItemNotFoundException(
                    "Cart item does not belong to this cart.");
        }

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        log.info(
                "Cart item {} updated for user {}.",
                cartItemId);

        return cartMapper.toCartResponseDTO(cart);
    }

    @Override
    public DeleteCartResponseDTO removeCartItem(
            Long cartItemId) {

        User user = currentUserService.getCurrentUser();

        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Cart item not found."));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {

            throw new CartItemNotFoundException(
                    "Cart item does not belong to this cart.");
        }

        cartItemRepository.delete(cartItem);

        log.info(
                "Cart item {} removed from user {} cart.",
                cartItemId);

        return DeleteCartResponseDTO.builder()
                .state("REMOVED")
                .build();
    }

    @Override
    public DeleteCartResponseDTO clearCart() {

        User user = currentUserService.getCurrentUser();

        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found."));

        cart.getCartItems().clear();

        cartRepository.save(cart);

        log.info(
                "Cart cleared for user {}.");

        return DeleteCartResponseDTO.builder()
                .state("CLEARED")
                .build();
    }


}