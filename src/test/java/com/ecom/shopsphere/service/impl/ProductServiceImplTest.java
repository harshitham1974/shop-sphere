package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteProductResponseDTO;
import com.ecom.shopsphere.dto.response.ProductResponseDTO;
import com.ecom.shopsphere.entity.Category;
import com.ecom.shopsphere.entity.Product;
import com.ecom.shopsphere.exception.CategoryNotFoundException;
import com.ecom.shopsphere.exception.ProductNotFoundException;
import com.ecom.shopsphere.mapper.ProductMapper;
import com.ecom.shopsphere.repository.CategoryRepository;
import com.ecom.shopsphere.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryRepository categoryRepository;


    @Test
    void createProduct_Success() {

        // Arrange

        CreateProductRequestDTO request = CreateProductRequestDTO.builder().productName("Laptop").categoryId(1L).build();


        Category category = Category.builder().categoryId(1L).categoryName("Electronics").build();


        Product product = Product.builder().productId(1L).productName("Laptop").category(category).build();


        Product savedProduct = Product.builder().productId(1L).productName("Laptop").category(category).build();


        ProductResponseDTO response = ProductResponseDTO.builder().productId(1L).productName("Laptop").build();


        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));


        when(productMapper.toEntity(request, category)).thenReturn(product);


        when(productRepository.save(product)).thenReturn(savedProduct);


        when(productMapper.toResponse(savedProduct)).thenReturn(response);


        // Act

        ProductResponseDTO result = productService.createProduct(request);


        // Assert

        assertNotNull(result);

        assertEquals(1L, result.getProductId());

        assertEquals("Laptop", result.getProductName());


        verify(categoryRepository).findById(1L);

        verify(productMapper).toEntity(request, category);

        verify(productRepository).save(product);

        verify(productMapper).toResponse(savedProduct);
    }

    @Test
    void createProduct_CategoryNotFound() {

        // Arrange

        CreateProductRequestDTO request = CreateProductRequestDTO.builder().productName("Laptop").categoryId(99L).build();


        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());


        // Act & Assert

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> productService.createProduct(request));


        assertEquals("Category not found.", exception.getMessage());


        // Verify

        verify(categoryRepository).findById(99L);

        verify(productMapper, never()).toEntity(any(CreateProductRequestDTO.class), any(Category.class));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {

        // Arrange

        Long productId = 1L;


        Product product = Product.builder().productId(productId).productName("Laptop").build();


        ProductResponseDTO response = ProductResponseDTO.builder().productId(productId).productName("Laptop").build();


        when(productRepository.findById(productId)).thenReturn(Optional.of(product));


        when(productMapper.toResponse(product)).thenReturn(response);


        // Act

        ProductResponseDTO result = productService.getProductById(productId);


        // Assert

        assertNotNull(result);

        assertEquals(productId, result.getProductId());

        assertEquals("Laptop", result.getProductName());


        // Verify

        verify(productRepository).findById(productId);

        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductById_ProductNotFound() {

        // Arrange

        Long productId = 99L;


        when(productRepository.findById(productId)).thenReturn(Optional.empty());


        // Act & Assert

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));


        assertEquals("Product with ID 99 does not exist.", exception.getMessage());


        // Verify

        verify(productRepository).findById(productId);

        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void getAllProducts_Success() {

        // Arrange

        Product product1 = Product.builder().productId(1L).productName("Laptop").build();


        Product product2 = Product.builder().productId(2L).productName("Phone").build();


        ProductResponseDTO response1 = ProductResponseDTO.builder().productId(1L).productName("Laptop").build();


        ProductResponseDTO response2 = ProductResponseDTO.builder().productId(2L).productName("Phone").build();


        when(productRepository.findAll()).thenReturn(List.of(product1, product2));


        when(productMapper.toResponse(product1)).thenReturn(response1);


        when(productMapper.toResponse(product2)).thenReturn(response2);


        // Act

        List<ProductResponseDTO> result = productService.getAllProducts();


        // Assert

        assertNotNull(result);

        assertEquals(2, result.size());


        assertEquals(1L, result.get(0).getProductId());


        assertEquals("Laptop", result.get(0).getProductName());


        assertEquals(2L, result.get(1).getProductId());


        assertEquals("Phone", result.get(1).getProductName());


        // Verify

        verify(productRepository).findAll();


        verify(productMapper).toResponse(product1);


        verify(productMapper).toResponse(product2);
    }

    @Test
    void updateProduct_Success() {

        // Arrange

        Long productId = 1L;


        UpdateProductRequestDTO request = UpdateProductRequestDTO.builder().productName("Updated Laptop").categoryId(2L).build();


        Category category = Category.builder().categoryId(2L).categoryName("Electronics").build();


        Product product = Product.builder().productId(productId).productName("Laptop").build();


        Product updatedProduct = Product.builder().productId(productId).productName("Updated Laptop").category(category).build();


        ProductResponseDTO response = ProductResponseDTO.builder().productId(productId).productName("Updated Laptop").build();


        when(productRepository.findById(productId)).thenReturn(Optional.of(product));


        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));


        when(productRepository.save(product)).thenReturn(updatedProduct);


        when(productMapper.toResponse(updatedProduct)).thenReturn(response);


        // Act

        ProductResponseDTO result = productService.updateProduct(productId, request);


        // Assert

        assertNotNull(result);

        assertEquals(productId, result.getProductId());

        assertEquals("Updated Laptop", result.getProductName());


        // Verify

        verify(productRepository).findById(productId);

        verify(categoryRepository).findById(2L);

        verify(productMapper).updateProductFromRequest(request, product, category);

        verify(productRepository).save(product);

        verify(productMapper).toResponse(updatedProduct);
    }

    @Test
    void updateProduct_ProductNotFound() {

        // Arrange

        Long productId = 99L;

        UpdateProductRequestDTO request = UpdateProductRequestDTO.builder().productName("Updated Laptop").categoryId(2L).build();


        when(productRepository.findById(productId)).thenReturn(Optional.empty());


        // Act & Assert

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productId, request));


        assertEquals("Product with ID 99 does not exist.", exception.getMessage());


        // Verify

        verify(productRepository).findById(productId);

        verify(categoryRepository, never()).findById(anyLong());

        verify(productMapper, never()).updateProductFromRequest(any(UpdateProductRequestDTO.class), any(Product.class), any(Category.class));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_CategoryNotFound() {

        // Arrange

        Long productId = 1L;


        UpdateProductRequestDTO request = UpdateProductRequestDTO.builder().productName("Updated Laptop").categoryId(99L).build();


        Product product = Product.builder().productId(productId).productName("Laptop").build();


        when(productRepository.findById(productId)).thenReturn(Optional.of(product));


        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());


        // Act & Assert

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> productService.updateProduct(productId, request));


        assertEquals("Category not found.", exception.getMessage());


        // Verify

        verify(productRepository).findById(productId);

        verify(categoryRepository).findById(99L);

        verify(productMapper, never()).updateProductFromRequest(any(UpdateProductRequestDTO.class), any(Product.class), any(Category.class));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {

        // Arrange

        Long productId = 1L;


        Product product = Product.builder().productId(productId).productName("Laptop").build();


        when(productRepository.findById(productId)).thenReturn(Optional.of(product));


        // Act

        DeleteProductResponseDTO result = productService.deleteProduct(productId);


        // Assert

        assertNotNull(result);

        assertEquals(productId, result.getProductId());

        assertEquals("Laptop", result.getProductName());

        assertEquals("DELETED", result.getState());


        // Verify

        verify(productRepository).findById(productId);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_ProductNotFound() {

        // Arrange

        Long productId = 99L;


        when(productRepository.findById(productId)).thenReturn(Optional.empty());


        // Act & Assert

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId));


        assertEquals("Product with ID 99 does not exist.", exception.getMessage());


        // Verify

        verify(productRepository).findById(productId);

        verify(productRepository, never()).delete(any(Product.class));
    }
}