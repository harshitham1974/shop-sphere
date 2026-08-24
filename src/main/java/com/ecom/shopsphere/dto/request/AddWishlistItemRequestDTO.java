package com.ecom.shopsphere.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddWishlistItemRequestDTO {

    @NotNull(message = "Product ID is required.")
    private Long productId;
}