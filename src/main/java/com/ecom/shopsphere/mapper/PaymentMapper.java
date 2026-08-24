package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.response.PaymentResponseDTO;
import com.ecom.shopsphere.entity.Payment;

public interface PaymentMapper {

    PaymentResponseDTO toResponse(Payment payment);

}