package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.CategoryResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCategoryResponseDTO;
import com.ecom.shopsphere.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Category Management",
        description = "APIs for managing product categories including creation, retrieval, update, and deletion."
)
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Create a category",
            description = "Creates a new product category. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "409", description = "Category already exists")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO request) {

        CategoryResponseDTO response =
                categoryService.createCategory(request);

        ApiResponseDTO<CategoryResponseDTO> apiResponseDTO =
                ApiResponseDTO.<CategoryResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Category created successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDTO);
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a specific category by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> getCategoryById(
            @PathVariable Long categoryId) {

        CategoryResponseDTO response =
                categoryService.getCategoryById(categoryId);

        ApiResponseDTO<CategoryResponseDTO> apiResponseDTO =
                ApiResponseDTO.<CategoryResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get all categories",
            description = "Retrieves a list of all product categories."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories fetched successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getAllCategories() {

        List<CategoryResponseDTO> response =
                categoryService.getAllCategories();

        ApiResponseDTO<List<CategoryResponseDTO>> apiResponseDTO =
                ApiResponseDTO.<List<CategoryResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Categories fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Update a category",
            description = "Updates an existing category. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequestDTO request) {

        CategoryResponseDTO response =
                categoryService.updateCategory(categoryId, request);

        ApiResponseDTO<CategoryResponseDTO> apiResponseDTO =
                ApiResponseDTO.<CategoryResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category updated successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes an existing category. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<DeleteCategoryResponseDTO>> deleteCategory(
            @PathVariable Long categoryId) {

        DeleteCategoryResponseDTO response =
                categoryService.deleteCategory(categoryId);

        ApiResponseDTO<DeleteCategoryResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteCategoryResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
