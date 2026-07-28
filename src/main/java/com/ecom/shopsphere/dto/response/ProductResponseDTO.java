package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long productId;

    private String productName;

    private String description;

    private String brand;

    private Long categoryId;

    private String categoryName;

    private BigDecimal price;

    private Integer stockQuantity;

    private String imageUrl;
}