package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddCartItemRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCartItemRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.CartResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCartResponseDTO;
import com.ecom.shopsphere.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Cart Management",
        description = "APIs for managing the shopping cart including adding, updating, removing items, and clearing the cart."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "Add item to cart",
            description = "Adds a product to the user's shopping cart. If the item already exists, the quantity is updated. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/items")
    public ResponseEntity<ApiResponseDTO<CartResponseDTO>> addToCart(
            @Valid @RequestBody AddCartItemRequestDTO request) {

        log.info("Received request to add product {} to cart for user {}.",
                request.getProductId());

        CartResponseDTO response =
                cartService.addToCart(request);

        log.info("Product {} added to cart successfully for user {}.",
                request.getProductId());

        ApiResponseDTO<CartResponseDTO> apiResponseDTO =
                ApiResponseDTO.<CartResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Product added to cart successfully")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "Get cart",
            description = "Retrieves the current user's shopping cart with all items and totals. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<CartResponseDTO>> getCart() {

        log.info("Received request to fetch cart for user {}.");

        CartResponseDTO response =
                cartService.getCart();

        log.info("Cart fetched successfully for user {}.");

        ApiResponseDTO<CartResponseDTO> apiResponseDTO =
                ApiResponseDTO.<CartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Update cart item",
            description = "Updates the quantity of a specific item in the user's cart. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponseDTO<CartResponseDTO>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {

        log.info("Received request to update cart item {} for user {}.",
                cartItemId);

        CartResponseDTO response =
                cartService.updateCartItem(cartItemId, request);

        log.info("Cart item {} updated successfully for user {}.",
                cartItemId);

        ApiResponseDTO<CartResponseDTO> apiResponseDTO =
                ApiResponseDTO.<CartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Remove cart item",
            description = "Removes a specific item from the user's shopping cart. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponseDTO<DeleteCartResponseDTO>> removeCartItem(
            @PathVariable Long cartItemId) {

        log.info("Received request to remove cart item {} for user {}.",
                cartItemId);

        DeleteCartResponseDTO response = cartService.removeCartItem(cartItemId);

        log.info("Cart item {} removed successfully for user {}.",
                cartItemId);

        ApiResponseDTO<DeleteCartResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteCartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart item removed successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Clear cart",
            description = "Removes all items from the user's shopping cart. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @DeleteMapping
    public ResponseEntity<ApiResponseDTO<DeleteCartResponseDTO>> clearCart() {

        log.info("Received request to clear cart for user {}.");

        DeleteCartResponseDTO response = cartService.clearCart();

        log.info("Cart cleared successfully for user {}.");

        ApiResponseDTO<DeleteCartResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteCartResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cart cleared successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

}
