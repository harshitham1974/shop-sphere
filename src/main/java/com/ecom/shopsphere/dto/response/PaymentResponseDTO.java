package com.ecom.shopsphere.dto.response;

import com.ecom.shopsphere.entity.PaymentMethod;
import com.ecom.shopsphere.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {


    private Long paymentId;


    private Long orderId;


    private String transactionId;


    private BigDecimal amount;


    private PaymentMethod paymentMethod;


    private PaymentStatus paymentStatus;


    private LocalDateTime paymentDate;

}