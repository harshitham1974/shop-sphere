package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long orderItemId;

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal priceAtPurchase;

    private BigDecimal subtotal;
}