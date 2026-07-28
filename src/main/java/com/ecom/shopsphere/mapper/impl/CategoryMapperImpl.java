package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.mapper.CategoryMapper;
import org.springframework.stereotype.Component;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.dto.response.CategoryResponseDTO;
import com.ecom.shopsphere.entity.Category;

@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toEntity(CreateCategoryRequestDTO request) {

        return Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();
    }

    @Override
    public CategoryResponseDTO toResponse(Category category) {

        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    @Override
    public void updateCategoryFromRequest(
            UpdateCategoryRequestDTO request,
            Category category) {

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
    }
}