package com.ecom.shopsphere.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDTO {

    @NotNull(message = "Shipping address is required.")
    private Long shippingAddressId;
}