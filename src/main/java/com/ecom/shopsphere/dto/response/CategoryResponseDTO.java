package com.ecom.shopsphere.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponseDTO {

    private Long categoryId;

    private String categoryName;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}