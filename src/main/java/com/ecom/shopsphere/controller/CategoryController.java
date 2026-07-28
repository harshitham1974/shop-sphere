package com.ecom.shopsphere.controller;

import java.util.List;

import com.ecom.shopsphere.dto.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO request) {

        CategoryResponseDTO response =
                categoryService.createCategory(request);

        ApiResponse<CategoryResponseDTO> apiResponse =
                ApiResponse.<CategoryResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Category created successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(
            @PathVariable Long categoryId) {

        CategoryResponseDTO response =
                categoryService.getCategoryById(categoryId);

        ApiResponse<CategoryResponseDTO> apiResponse =
                ApiResponse.<CategoryResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {

        List<CategoryResponseDTO> response =
                categoryService.getAllCategories();

        ApiResponse<List<CategoryResponseDTO> > apiResponse =
                ApiResponse.<List<CategoryResponseDTO> >builder()
                        .status(HttpStatus.OK.value())
                        .message("Categories fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequestDTO request) {

        CategoryResponseDTO response =
                categoryService.updateCategory(categoryId, request);

        ApiResponse<CategoryResponseDTO> apiResponse =
                ApiResponse.<CategoryResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category updated successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<DeleteCategoryResponseDTO>> deleteCategory(
            @PathVariable Long categoryId) {

        DeleteCategoryResponseDTO response =
                categoryService.deleteCategory(categoryId);

        ApiResponse<DeleteCategoryResponseDTO> apiResponse =
                ApiResponse.<DeleteCategoryResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Category deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }
}