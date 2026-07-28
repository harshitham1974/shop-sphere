package com.ecom.shopsphere.service;

import java.util.List;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.dto.response.CategoryResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCategoryResponseDTO;

public interface CategoryService {

    CategoryResponseDTO createCategory(
            CreateCategoryRequestDTO request);

    CategoryResponseDTO getCategoryById(
            Long categoryId);

    List<CategoryResponseDTO> getAllCategories();

    CategoryResponseDTO updateCategory(
            Long categoryId,
            UpdateCategoryRequestDTO request);

    DeleteCategoryResponseDTO deleteCategory(
            Long categoryId);
}