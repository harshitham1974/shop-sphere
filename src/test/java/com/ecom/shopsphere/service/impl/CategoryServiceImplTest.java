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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    void createCategory_Success() {
        CreateCategoryRequestDTO request = CreateCategoryRequestDTO.builder()
                .categoryName("Electronics")
                .description("Electronic items")
                .build();

        Category category = Category.builder().categoryName("Electronics").description("Electronic items").build();
        Category savedCategory = Category.builder().categoryId(1L).categoryName("Electronics").description("Electronic items").build();
        CategoryResponseDTO response = CategoryResponseDTO.builder().categoryId(1L).categoryName("Electronics").build();

        when(categoryRepository.existsByCategoryNameIgnoreCase("Electronics")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        CategoryResponseDTO result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());

        verify(categoryRepository).existsByCategoryNameIgnoreCase("Electronics");
        verify(categoryMapper).toEntity(request);
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_AlreadyExists_ThrowsException() {
        CreateCategoryRequestDTO request = CreateCategoryRequestDTO.builder()
                .categoryName("ExistingCategory")
                .build();

        when(categoryRepository.existsByCategoryNameIgnoreCase("ExistingCategory")).thenReturn(true);

        CategoryAlreadyExistsException exception = assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Category 'ExistingCategory' already exists.", exception.getMessage());

        verify(categoryRepository).existsByCategoryNameIgnoreCase("ExistingCategory");
        verify(categoryMapper, never()).toEntity(any(CreateCategoryRequestDTO.class));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_Success() {
        Long categoryId = 1L;
        Category category = Category.builder().categoryId(categoryId).categoryName("Books").build();
        CategoryResponseDTO response = CategoryResponseDTO.builder().categoryId(categoryId).categoryName("Books").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponseDTO result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryId());
        assertEquals("Books", result.getCategoryName());

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryById_NotFound() {
        Long categoryId = 99L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(categoryId));

        assertEquals("Category with ID 99 does not exist.", exception.getMessage());
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    void getAllCategories_Success() {
        Category category1 = Category.builder().categoryId(1L).categoryName("Electronics").build();
        Category category2 = Category.builder().categoryId(2L).categoryName("Books").build();
        CategoryResponseDTO response1 = CategoryResponseDTO.builder().categoryId(1L).categoryName("Electronics").build();
        CategoryResponseDTO response2 = CategoryResponseDTO.builder().categoryId(2L).categoryName("Books").build();

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));
        when(categoryMapper.toResponse(category1)).thenReturn(response1);
        when(categoryMapper.toResponse(category2)).thenReturn(response2);

        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getCategoryName());
        assertEquals("Books", result.get(1).getCategoryName());

        verify(categoryRepository).findAll();
    }

    @Test
    void getAllCategories_Empty() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findAll();
    }

    @Test
    void updateCategory_Success() {
        Long categoryId = 1L;
        UpdateCategoryRequestDTO request = UpdateCategoryRequestDTO.builder()
                .categoryName("Updated Name")
                .description("Updated description")
                .build();

        Category category = Category.builder().categoryId(categoryId).categoryName("Old Name").build();
        Category updatedCategory = Category.builder().categoryId(categoryId).categoryName("Updated Name").build();
        CategoryResponseDTO response = CategoryResponseDTO.builder().categoryId(categoryId).categoryName("Updated Name").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryNameIgnoreCase("Updated Name")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(response);

        CategoryResponseDTO result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Updated Name", result.getCategoryName());

        verify(categoryMapper).updateCategoryFromRequest(request, category);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_SameName_Success() {
        Long categoryId = 1L;
        UpdateCategoryRequestDTO request = UpdateCategoryRequestDTO.builder()
                .categoryName("Same Name")
                .description("Updated description")
                .build();

        Category category = Category.builder().categoryId(categoryId).categoryName("Same Name").build();
        Category updatedCategory = Category.builder().categoryId(categoryId).categoryName("Same Name").build();
        CategoryResponseDTO response = CategoryResponseDTO.builder().categoryId(categoryId).categoryName("Same Name").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(response);

        CategoryResponseDTO result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Same Name", result.getCategoryName());

        verify(categoryRepository, never()).existsByCategoryNameIgnoreCase(anyString());
    }

    @Test
    void updateCategory_DifferentNameAlreadyExists_ThrowsException() {
        Long categoryId = 1L;
        UpdateCategoryRequestDTO request = UpdateCategoryRequestDTO.builder()
                .categoryName("ExistingCategory")
                .build();

        Category category = Category.builder().categoryId(categoryId).categoryName("Old Name").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryNameIgnoreCase("ExistingCategory")).thenReturn(true);

        CategoryAlreadyExistsException exception = assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Category 'ExistingCategory' already exists.", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).updateCategoryFromRequest(any(), any());
    }

    @Test
    void updateCategory_NotFound() {
        Long categoryId = 99L;
        UpdateCategoryRequestDTO request = UpdateCategoryRequestDTO.builder().categoryName("Test").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Category with ID 99 does not exist.", exception.getMessage());
        verify(categoryRepository, never()).existsByCategoryNameIgnoreCase(anyString());
    }

    @Test
    void deleteCategory_Success() {
        Long categoryId = 1L;
        Category category = Category.builder().categoryId(categoryId).categoryName("Electronics").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        DeleteCategoryResponseDTO result = categoryService.deleteCategory(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());
        assertEquals("Category deleted successfully.", result.getState());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_NotFound() {
        Long categoryId = 99L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(categoryId));

        assertEquals("Category with ID 99 does not exist.", exception.getMessage());
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
