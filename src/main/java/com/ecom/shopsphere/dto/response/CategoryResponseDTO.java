package com.ecom.shopsphere.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDTO {

    private Long categoryId;

    private String categoryName;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}