package com.ecom.shopsphere.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteCategoryResponseDTO {

    private Long categoryId;

    private String categoryName;

    private String state;
}