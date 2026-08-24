package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.dto.response.PaymentResponseDTO;
import com.ecom.shopsphere.entity.Payment;
import com.ecom.shopsphere.mapper.PaymentMapper;

import org.springframework.stereotype.Component;


@Component
public class PaymentMapperImpl implements PaymentMapper {


    @Override
    public PaymentResponseDTO toResponse(Payment payment) {

        return PaymentResponseDTO.builder()

                .paymentId(payment.getPaymentId())

                .orderId(payment.getOrder().getOrderId())

                .transactionId(payment.getTransactionId())

                .amount(payment.getAmount())

                .paymentMethod(payment.getPaymentMethod())

                .paymentStatus(payment.getPaymentStatus())

                .paymentDate(payment.getPaymentDate())

                .build();
    }
}