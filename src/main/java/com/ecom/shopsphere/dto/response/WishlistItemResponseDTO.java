package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemResponseDTO {

    private Long wishlistItemId;

    private Long productId;

    private String productName;

    private String description;

    private String brand;

    private BigDecimal price;

    private String imageUrl;

    private String categoryName;

    private Boolean inStock;
}