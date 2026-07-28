package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.dto.response.CategoryResponseDTO;
import com.ecom.shopsphere.entity.Category;

public interface CategoryMapper {

    Category toEntity(CreateCategoryRequestDTO request);

    CategoryResponseDTO toResponse(Category category);

    void updateCategoryFromRequest(
            UpdateCategoryRequestDTO request,
            Category category);
}