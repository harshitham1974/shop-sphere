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
import com.ecom.shopsphere.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponseDTO createProduct(
            CreateProductRequestDTO request) {

        log.info("Starting product creation.");

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found."));

        log.info("Mapping CreateProductRequestDTO to Product entity.");

        Product product =
                productMapper.toEntity(request, category);

        log.debug("Product entity created successfully.");

        Product savedProduct =
                productRepository.save(product);

        log.info(
                "Product created successfully. Product ID: {}, Product Name: {}",
                savedProduct.getProductId(),
                savedProduct.getProductName()
        );

        return productMapper.toResponse(savedProduct);
    }
    @Override
    public ProductResponseDTO getProductById(
            Long productId) {

        log.info("Fetching product with ID: {}", productId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> {

                    log.error("Product not found with ID: {}", productId);

                    return new ProductNotFoundException(
                            "Product with ID " + productId + " does not exist.");
                });

        log.info("Product fetched successfully. Product ID: {}", productId);

        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {

        log.info("Fetching all products.");

        List<Product> products = productRepository.findAll();

        log.info("Total products found: {}", products.size());

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponseDTO updateProduct(
            Long productId,
            UpdateProductRequestDTO request) {

        log.info("Updating product with ID: {}", productId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> {

                    log.error("Product not found with ID: {}", productId);

                    return new ProductNotFoundException(
                            "Product with ID " + productId + " does not exist.");
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found."));

        productMapper.updateProductFromRequest(request, product, category);

        Product updatedProduct =
                productRepository.save(product);

        log.info("Product updated successfully. Product ID: {}", productId);

        return productMapper.toResponse(updatedProduct);
    }
    @Override
    public DeleteProductResponseDTO deleteProduct(Long productId) {

        log.info("Deleting product with ID: {}", productId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> {

                    log.error("Product not found with ID: {}", productId);

                    return new ProductNotFoundException(
                            "Product with ID " + productId + " does not exist.");
                });

        DeleteProductResponseDTO response =
                DeleteProductResponseDTO.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .state("DELETED")
                        .build();

        productRepository.delete(product);

        log.info("Product deleted successfully. Product ID: {}", productId);

        return response;
    }

}