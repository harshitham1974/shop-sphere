package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteProductResponseDTO;
import com.ecom.shopsphere.dto.response.ProductResponseDTO;
import com.ecom.shopsphere.service.ProductService;
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
        name = "Product Management",
        description = "APIs for managing products including creation, retrieval, update, and deletion."
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create a product",
            description = "Creates a new product. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @Valid @RequestBody CreateProductRequestDTO request) {

        log.info("Received request to create product.");

        ProductResponseDTO response =
                productService.createProduct(request);

        log.info("Product created successfully.");

        ApiResponseDTO<ProductResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ProductResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Product created successfully")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a specific product by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductById(
            @PathVariable Long productId) {

        log.info("Received request to fetch product with ID: {}", productId);

        ProductResponseDTO response =
                productService.getProductById(productId);

        log.info("Returning product with ID: {}", productId);

        ApiResponseDTO<ProductResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ProductResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Product fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieves a list of all available products."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products fetched successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> getAllProducts() {

        log.info("Received request to fetch all products.");

        List<ProductResponseDTO> response =
                productService.getAllProducts();

        log.info("Returning {} products.", response.size());

        ApiResponseDTO<List<ProductResponseDTO>> apiResponseDTO =
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Products fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Update a product",
            description = "Updates an existing product. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product or category not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequestDTO request) {

        log.info("Received request to update product with ID: {}", productId);

        ProductResponseDTO response =
                productService.updateProduct(productId, request);

        log.info("Returning updated product with ID: {}", productId);

        ApiResponseDTO<ProductResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ProductResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Product updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes an existing product. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseDTO<DeleteProductResponseDTO>> deleteProduct(
            @PathVariable Long productId) {

        log.info("Received request to delete product with ID: {}", productId);

        DeleteProductResponseDTO response = productService.deleteProduct(productId);

        log.info("Product deleted successfully. Product ID: {}", productId);

        ApiResponseDTO<DeleteProductResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteProductResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Product deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

}
