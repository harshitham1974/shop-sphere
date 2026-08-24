package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.CreateCategoryRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateCategoryRequestDTO;
import com.ecom.shopsphere.dto.response.CategoryResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteCategoryResponseDTO;
import com.ecom.shopsphere.entity.Category;
import com.ecom.shopsphere.exception.CategoryAlreadyExistsException;
import com.ecom.shopsphere.exception.CategoryNotFoundException;
import com.ecom.shopsphere.mapper.CategoryMapper;
import com.ecom.shopsphere.repository.CategoryRepository;
import com.ecom.shopsphere.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO createCategory(
            CreateCategoryRequestDTO request) {

        log.info("Creating category with name: {}", request.getCategoryName());

        if (categoryRepository.existsByCategoryNameIgnoreCase(
                request.getCategoryName())) {

            log.error("Category already exists: {}", request.getCategoryName());

            throw new CategoryAlreadyExistsException(
                    "Category '" + request.getCategoryName() + "' already exists.");
        }

        Category category = categoryMapper.toEntity(request);

        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully. Category ID: {}",
                savedCategory.getCategoryId());

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long categoryId) {

        log.info("Fetching category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {

                    log.error("Category not found with ID: {}", categoryId);

                    return new CategoryNotFoundException(
                            "Category with ID " + categoryId + " does not exist.");
                });

        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {

        log.info("Fetching all categories.");

        List<Category> categories = categoryRepository.findAll();

        log.info("Total categories found: {}", categories.size());

        return categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponseDTO updateCategory(
            Long categoryId,
            UpdateCategoryRequestDTO request) {

        log.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {

                    log.error("Category not found with ID: {}", categoryId);

                    return new CategoryNotFoundException(
                            "Category with ID " + categoryId + " does not exist.");
                });

        if (!category.getCategoryName().equalsIgnoreCase(request.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(
                request.getCategoryName())) {

            log.error("Category already exists: {}", request.getCategoryName());

            throw new CategoryAlreadyExistsException(
                    "Category '" + request.getCategoryName() + "' already exists.");
        }

        categoryMapper.updateCategoryFromRequest(request, category);

        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully. Category ID: {}",
                updatedCategory.getCategoryId());

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public DeleteCategoryResponseDTO deleteCategory(Long categoryId) {

        log.info("Deleting category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {

                    log.error("Category not found with ID: {}", categoryId);

                    return new CategoryNotFoundException(
                            "Category with ID " + categoryId + " does not exist.");
                });

        DeleteCategoryResponseDTO response =
                DeleteCategoryResponseDTO.builder()
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getCategoryName())
                        .state("Category deleted successfully.")
                        .build();

        categoryRepository.delete(category);

        log.info("Category deleted successfully. Category ID: {}", categoryId);

        return response;
    }
}