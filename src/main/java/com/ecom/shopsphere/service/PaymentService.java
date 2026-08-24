package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.CreatePaymentRequestDTO;
import com.ecom.shopsphere.dto.response.PaymentResponseDTO;

public interface PaymentService {


    PaymentResponseDTO createPayment(
            CreatePaymentRequestDTO request);


    PaymentResponseDTO getPaymentByOrderId(
            Long orderId);

}