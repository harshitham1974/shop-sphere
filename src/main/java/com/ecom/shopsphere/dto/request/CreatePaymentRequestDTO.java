package com.ecom.shopsphere.dto.request;

import com.ecom.shopsphere.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequestDTO {


    @NotNull(message = "Order ID is required.")
    private Long orderId;


    @NotNull(message = "Payment method is required.")
    private PaymentMethod paymentMethod;

}