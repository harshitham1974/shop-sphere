package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.dto.response.DeleteProductResponseDTO;
import com.ecom.shopsphere.dto.response.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(CreateProductRequestDTO request);

    ProductResponseDTO getProductById(Long productId);

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO updateProduct(
            Long productId,
            UpdateProductRequestDTO request);

    DeleteProductResponseDTO deleteProduct(Long productId);

}