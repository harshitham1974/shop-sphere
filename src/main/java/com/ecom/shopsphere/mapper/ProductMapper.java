package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.request.CreateProductRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProductRequestDTO;
import com.ecom.shopsphere.dto.response.ProductResponseDTO;
import com.ecom.shopsphere.entity.Category;
import com.ecom.shopsphere.entity.Product;

public interface ProductMapper {

    Product toEntity(
            CreateProductRequestDTO request,
            Category category);

    ProductResponseDTO toResponse(Product product);

    void updateProductFromRequest(
            UpdateProductRequestDTO request,
            Product product,
            Category category);
}