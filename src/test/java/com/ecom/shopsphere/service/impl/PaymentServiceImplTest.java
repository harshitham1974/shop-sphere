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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Test
    void createPayment_Success_UPI() {
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(1L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        Order order = Order.builder()
                .orderId(1L)
                .orderNumber("ORD-001")
                .totalAmount(new BigDecimal("5000.00"))
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = Payment.builder()
                .paymentId(1L)
                .transactionId("TXN-ABC12345")
                .amount(new BigDecimal("5000.00"))
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .order(order)
                .build();

        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .paymentId(1L)
                .transactionId("TXN-ABC12345")
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toResponse(savedPayment)).thenReturn(response);

        PaymentResponseDTO result = paymentService.createPayment(request);

        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());

        verify(orderRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(order);
        assertEquals(PaymentStatus.SUCCESS, order.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
    }

    @Test
    void createPayment_Success_CreditCard() {
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        Order order = Order.builder()
                .orderId(1L)
                .totalAmount(new BigDecimal("10000.00"))
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = Payment.builder()
                .paymentId(1L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .paymentId(1L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toResponse(savedPayment)).thenReturn(response);

        PaymentResponseDTO result = paymentService.createPayment(request);

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
    }

    @Test
    void createPayment_OrderNotFound_ThrowsException() {
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(99L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> paymentService.createPayment(request));

        assertEquals("Order not found.", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_COD_ThrowsPaymentFailedException() {
        CreatePaymentRequestDTO request = CreatePaymentRequestDTO.builder()
                .orderId(1L)
                .paymentMethod(PaymentMethod.COD)
                .build();

        Order order = Order.builder()
                .orderId(1L)
                .totalAmount(new BigDecimal("5000.00"))
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Payment failedPayment = Payment.builder()
                .paymentId(1L)
                .paymentStatus(PaymentStatus.FAILED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);

        PaymentFailedException exception = assertThrows(PaymentFailedException.class,
                () -> paymentService.createPayment(request));

        assertEquals("Payment could not be completed.", exception.getMessage());

        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getPaymentByOrderId_Success() {
        Long orderId = 1L;
        Payment payment = Payment.builder()
                .paymentId(1L)
                .transactionId("TXN-001")
                .amount(new BigDecimal("5000.00"))
                .build();
        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .paymentId(1L)
                .transactionId("TXN-001")
                .build();

        when(paymentRepository.findByOrderOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponseDTO result = paymentService.getPaymentByOrderId(orderId);

        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
        assertEquals("TXN-001", result.getTransactionId());

        verify(paymentRepository).findByOrderOrderId(orderId);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void getPaymentByOrderId_NotFound() {
        Long orderId = 99L;

        when(paymentRepository.findByOrderOrderId(orderId)).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(orderId));

        assertEquals("Payment not found.", exception.getMessage());
        verify(paymentMapper, never()).toResponse(any(Payment.class));
    }
}
