package com.ecom.shopsphere.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteProductResponseDTO {

    private Long productId;
    private String productName;
    private String state;
}