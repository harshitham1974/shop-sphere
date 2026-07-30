package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponse;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCartResponseDTO;
import com.ecom.shopsphere.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addToCart(
            @Valid @RequestBody AddCartItemRequestDTO request) {

        log.info("Received request to add product {} to cart for user {}.",
                request.getProductId());

        CartResponseDTO response =
                cartService.addToCart(request);

        log.info("Product {} added to cart successfully for user {}.",
                request.getProductId());

        ApiResponse<CartResponseDTO> apiResponse =
                ApiResponse.<CartResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Product added to cart successfully")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart() {

        log.info("Received request to fetch cart for user {}.");

        CartResponseDTO response =
                cartService.getCart();

        log.info("Cart fetched successfully for user {}.");

        ApiResponse<CartResponseDTO> apiResponse =
                ApiResponse.<CartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {

        log.info("Received request to update cart item {} for user {}.",
                cartItemId);

        CartResponseDTO response =
                cartService.updateCartItem(cartItemId, request);

        log.info("Cart item {} updated successfully for user {}.",
                cartItemId);

        ApiResponse<CartResponseDTO> apiResponse =
                ApiResponse.<CartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<DeleteCartResponseDTO>> removeCartItem(
            @PathVariable Long cartItemId) {

        log.info("Received request to remove cart item {} for user {}.",
                cartItemId);

        DeleteCartResponseDTO response=cartService.removeCartItem( cartItemId);

        log.info("Cart item {} removed successfully for user {}.",
                cartItemId);

        ApiResponse<DeleteCartResponseDTO> apiResponse =
                ApiResponse.<DeleteCartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart item removed successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<DeleteCartResponseDTO>> clearCart() {

        log.info("Received request to clear cart for user {}.");

        DeleteCartResponseDTO response=cartService.clearCart();

        log.info("Cart cleared successfully for user {}.");

        ApiResponse<DeleteCartResponseDTO> apiResponse =
                ApiResponse.<DeleteCartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart cleared successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

}