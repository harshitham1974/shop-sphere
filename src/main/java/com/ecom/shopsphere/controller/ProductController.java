package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponse;
import com.ecom.shopsphere.dto.response.DeleteAccountResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteProductResponseDTO;
import com.ecom.shopsphere.dto.response.ProductResponseDTO;
import com.ecom.shopsphere.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @Valid @RequestBody CreateProductRequestDTO request) {

        log.info("Received request to create product.");

        ProductResponseDTO response =
                productService.createProduct(request);

        log.info("Product created successfully.");

        ApiResponse<ProductResponseDTO> apiResponse =
                ApiResponse.<ProductResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Product created successfully")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(
            @PathVariable Long productId) {

        log.info("Received request to fetch product with ID: {}", productId);

        ProductResponseDTO response =
                productService.getProductById(productId);

        log.info("Returning product with ID: {}", productId);

        ApiResponse<ProductResponseDTO> apiResponse =
                ApiResponse.<ProductResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Product fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllProducts() {

        log.info("Received request to fetch all products.");

        List<ProductResponseDTO> response =
                productService.getAllProducts();

        log.info("Returning {} products.", response.size());

        ApiResponse<List<ProductResponseDTO>> apiResponse =
                ApiResponse.<List<ProductResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Products fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequestDTO request) {

        log.info("Received request to update product with ID: {}", productId);

        ProductResponseDTO response =
                productService.updateProduct(productId, request);

        log.info("Returning updated product with ID: {}", productId);

        ApiResponse<ProductResponseDTO> apiResponse =
                ApiResponse.<ProductResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Product updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<DeleteProductResponseDTO>> deleteProduct(
            @PathVariable Long productId) {

        log.info("Received request to delete product with ID: {}", productId);

        DeleteProductResponseDTO response =productService.deleteProduct(productId);

        log.info("Product deleted successfully. Product ID: {}", productId);

        ApiResponse<DeleteProductResponseDTO> apiResponse =
                ApiResponse.<DeleteProductResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Product deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

}