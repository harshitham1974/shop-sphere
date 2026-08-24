package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateReviewRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateReviewRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteReviewResponseDTO;
import com.ecom.shopsphere.dto.response.ReviewResponseDTO;
import com.ecom.shopsphere.service.ReviewService;
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

import java.util.List;

@Tag(
        name = "Review Management",
        description = "APIs for managing product reviews including creation, retrieval, update, and deletion."
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Create a product review",
            description = "Creates a new review for a product. Requires authentication. A user can only review each product once."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Review already exists for this product by current user")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> createReview(
            @Valid @RequestBody CreateReviewRequestDTO request) {

        log.info("Received request to create review for product ID: {}", request.getProductId());

        ReviewResponseDTO response = reviewService.createReview(request);

        log.info("Review created successfully. Review ID: {}", response.getReviewId());

        ApiResponseDTO<ReviewResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ReviewResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Review created successfully")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "Get review by ID",
            description = "Retrieves a specific review by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> getReviewById(
            @PathVariable Long reviewId) {

        log.info("Received request to fetch review with ID: {}", reviewId);

        ReviewResponseDTO response = reviewService.getReviewById(reviewId);

        log.info("Returning review with ID: {}", reviewId);

        ApiResponseDTO<ReviewResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ReviewResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Review fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get reviews by product",
            description = "Retrieves all reviews for a specific product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews fetched successfully")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponseDTO<List<ReviewResponseDTO>>> getReviewsByProductId(
            @PathVariable Long productId) {

        log.info("Received request to fetch reviews for product ID: {}", productId);

        List<ReviewResponseDTO> response = reviewService.getReviewsByProductId(productId);

        log.info("Returning {} reviews for product ID: {}", response.size(), productId);

        ApiResponseDTO<List<ReviewResponseDTO>> apiResponseDTO =
                ApiResponseDTO.<List<ReviewResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Reviews fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get my reviews",
            description = "Retrieves all reviews submitted by the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponseDTO<List<ReviewResponseDTO>>> getMyReviews() {

        log.info("Received request to fetch reviews for current user.");

        List<ReviewResponseDTO> response = reviewService.getReviewsByCurrentUser();

        log.info("Returning {} reviews for current user.", response.size());

        ApiResponseDTO<List<ReviewResponseDTO>> apiResponseDTO =
                ApiResponseDTO.<List<ReviewResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Reviews fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Update a review",
            description = "Updates an existing review. Only the review owner can update it. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Review not found or not authorized to update")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequestDTO request) {

        log.info("Received request to update review with ID: {}", reviewId);

        ReviewResponseDTO response = reviewService.updateReview(reviewId, request);

        log.info("Returning updated review with ID: {}", reviewId);

        ApiResponseDTO<ReviewResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ReviewResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Review updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Delete a review",
            description = "Deletes an existing review. Only the review owner can delete it. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Review not found or not authorized to delete")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDTO<DeleteReviewResponseDTO>> deleteReview(
            @PathVariable Long reviewId) {

        log.info("Received request to delete review with ID: {}", reviewId);

        DeleteReviewResponseDTO response = reviewService.deleteReview(reviewId);

        log.info("Review deleted successfully. Review ID: {}", reviewId);

        ApiResponseDTO<DeleteReviewResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteReviewResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Review deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
