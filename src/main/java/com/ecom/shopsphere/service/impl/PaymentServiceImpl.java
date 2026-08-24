package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.CreatePaymentRequestDTO;
import com.ecom.shopsphere.dto.response.PaymentResponseDTO;
import com.ecom.shopsphere.entity.*;
import com.ecom.shopsphere.exception.OrderNotFoundException;
import com.ecom.shopsphere.exception.PaymentFailedException;
import com.ecom.shopsphere.exception.PaymentNotFoundException;
import com.ecom.shopsphere.mapper.PaymentMapper;
import com.ecom.shopsphere.repository.OrderRepository;
import com.ecom.shopsphere.repository.PaymentRepository;
import com.ecom.shopsphere.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    private final PaymentMapper paymentMapper;


    @Override
    @Transactional(noRollbackFor = PaymentFailedException.class)
    public PaymentResponseDTO createPayment(CreatePaymentRequestDTO request) {

        log.info("Creating payment for order ID: {}", request.getOrderId());

        Order order = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found.")
                );

        // Check whether payment already exists
        Payment payment = paymentRepository
                .findByOrderOrderId(order.getOrderId())
                .orElse(null);

        // Payment already completed
        if (payment != null &&
                payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

            throw new PaymentFailedException(
                    "Payment already completed for this order."
            );
        }

        // Create payment only if it doesn't exist
        if (payment == null) {

            payment = Payment.builder()
                    .transactionId(
                            "TXN-" + UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                    )
                    .amount(order.getTotalAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .order(order)
                    .build();
        } else {

            // Retry existing failed payment
            payment.setPaymentMethod(request.getPaymentMethod());
        }

        // Simulate payment failure
        if (request.getPaymentMethod() == PaymentMethod.COD) {

            payment.setPaymentStatus(PaymentStatus.FAILED);

            paymentRepository.save(payment);

            log.error(
                    "Payment failed for order ID: {}",
                    order.getOrderId()
            );

            throw new PaymentFailedException(
                    "Payment could not be completed."
            );
        }

        // Payment success
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setOrderStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);

        log.info(
                "Payment completed successfully. Transaction ID: {}",
                savedPayment.getTransactionId()
        );

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponseDTO getPaymentByOrderId(
            Long orderId) {


        Payment payment =
                paymentRepository
                        .findByOrderOrderId(orderId)
                        .orElseThrow(() -> new PaymentNotFoundException(
                                                "Payment not found."));


        return paymentMapper.toResponse(payment);
    }
}