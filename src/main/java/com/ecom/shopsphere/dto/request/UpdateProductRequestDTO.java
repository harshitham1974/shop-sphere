package com.ecom.shopsphere.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequestDTO {

    @NotBlank(message = "Product name is required.")
    private String productName;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Brand is required.")
    private String brand;

    @NotBlank(message = "Category is required.")
    private String category;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required.")
    @Positive(message = "Stock quantity must be greater than zero.")
    private Integer stockQuantity;

    @NotBlank(message = "Image URL is required.")
    private String imageUrl;
}