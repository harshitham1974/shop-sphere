package com.ecom.shopsphere.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private Long cartId;

    private List<CartItemResponseDTO> items;

    private Integer totalItems;

    private BigDecimal totalAmount;
}