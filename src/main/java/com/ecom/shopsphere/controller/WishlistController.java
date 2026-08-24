package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddWishlistItemRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteWishlistResponseDTO;
import com.ecom.shopsphere.dto.response.WishlistResponseDTO;
import com.ecom.shopsphere.service.WishlistService;
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
        name = "Wishlist Management",
        description = "APIs for managing the user's wishlist including adding, retrieving, and removing items."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(
            summary = "Add to wishlist",
            description = "Adds a product to the user's wishlist. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product added to wishlist successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/items")
    public ResponseEntity<ApiResponseDTO<WishlistResponseDTO>> addToWishlist(
            @Valid @RequestBody AddWishlistItemRequestDTO request) {

        log.info("Received request to add product to wishlist.");

        WishlistResponseDTO response =
                wishlistService.addToWishlist(request);

        ApiResponseDTO<WishlistResponseDTO> apiResponseDTO =
                ApiResponseDTO.<WishlistResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Product added to wishlist successfully.")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "Get wishlist",
            description = "Retrieves the current user's wishlist with all saved products. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Wishlist not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<WishlistResponseDTO>> getWishlist() {

        log.info("Received request to fetch wishlist.");

        WishlistResponseDTO response =
                wishlistService.getWishlist();

        ApiResponseDTO<WishlistResponseDTO> apiResponseDTO =
                ApiResponseDTO.<WishlistResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Wishlist fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Remove wishlist item",
            description = "Removes a specific product from the user's wishlist. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Wishlist item not found")
    })
    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<ApiResponseDTO<DeleteWishlistResponseDTO>> removeWishlistItem(
            @PathVariable Long wishlistItemId) {

        log.info(
                "Received request to remove wishlist item: {}",
                wishlistItemId);

        DeleteWishlistResponseDTO responseDTO = wishlistService.removeWishlistItem(wishlistItemId);

        ApiResponseDTO<DeleteWishlistResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteWishlistResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Wishlist item removed successfully.")
                        .data(responseDTO)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
