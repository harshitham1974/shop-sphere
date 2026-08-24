package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreatePaymentRequestDTO;
import com.ecom.shopsphere.dto.response.PaymentResponseDTO;
import com.ecom.shopsphere.exception.OrderNotFoundException;
import com.ecom.shopsphere.exception.PaymentFailedException;
import com.ecom.shopsphere.entity.PaymentMethod;
import com.ecom.shopsphere.exception.PaymentNotFoundException;
import com.ecom.shopsphere.entity.PaymentStatus;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void createPayment_Success() throws Exception {

        CreatePaymentRequestDTO request =
                CreatePaymentRequestDTO.builder()
                        .orderId(1L)
                        .paymentMethod(PaymentMethod.CREDIT_CARD)
                        .build();

        PaymentResponseDTO response =
                PaymentResponseDTO.builder()
                        .paymentId(1L)
                        .orderId(1L)
                        .transactionId("TXN-ABC123")
                        .amount(new BigDecimal("99.99"))
                        .paymentMethod(PaymentMethod.CREDIT_CARD)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .paymentDate(LocalDateTime.now())
                        .build();

        when(paymentService.createPayment(any(CreatePaymentRequestDTO.class)))
                .thenReturn(response);


        mockMvc.perform(
                        post("/api/v1/payments")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Payment completed successfully."))
                .andExpect(jsonPath("$.data.paymentId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.transactionId")
                        .value("TXN-ABC123"))
                .andExpect(jsonPath("$.data.amount")
                        .value(99.99))
                .andExpect(jsonPath("$.data.paymentMethod")
                        .value("CREDIT_CARD"))
                .andExpect(jsonPath("$.data.paymentStatus")
                        .value("SUCCESS"));

        verify(paymentService)
                .createPayment(any(CreatePaymentRequestDTO.class));
    }


    @Test
    void getPaymentByOrder_Success() throws Exception {

        Long orderId = 1L;

        PaymentResponseDTO response =
                PaymentResponseDTO.builder()
                        .paymentId(1L)
                        .orderId(orderId)
                        .transactionId("TXN-ABC123")
                        .amount(new BigDecimal("99.99"))
                        .paymentMethod(PaymentMethod.UPI)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .paymentDate(LocalDateTime.now())
                        .build();

        when(paymentService.getPaymentByOrderId(orderId))
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/v1/payments/order/{orderId}", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Payment fetched successfully."))
                .andExpect(jsonPath("$.data.paymentId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.transactionId")
                        .value("TXN-ABC123"))
                .andExpect(jsonPath("$.data.amount")
                        .value(99.99))
                .andExpect(jsonPath("$.data.paymentMethod")
                        .value("UPI"))
                .andExpect(jsonPath("$.data.paymentStatus")
                        .value("SUCCESS"));

        verify(paymentService)
                .getPaymentByOrderId(orderId);
    }


    @Test
    void createPayment_OrderNotFound() throws Exception {

        CreatePaymentRequestDTO request =
                CreatePaymentRequestDTO.builder()
                        .orderId(999L)
                        .paymentMethod(PaymentMethod.CREDIT_CARD)
                        .build();

        when(paymentService.createPayment(any(CreatePaymentRequestDTO.class)))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order Not Found"
                        )
                );


        mockMvc.perform(
                        post("/api/v1/payments")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(paymentService)
                .createPayment(any(CreatePaymentRequestDTO.class));
    }


    @Test
    void createPayment_PaymentFailed() throws Exception {

        CreatePaymentRequestDTO request =
                CreatePaymentRequestDTO.builder()
                        .orderId(1L)
                        .paymentMethod(PaymentMethod.CREDIT_CARD)
                        .build();

        when(paymentService.createPayment(any(CreatePaymentRequestDTO.class)))
                .thenThrow(
                        new PaymentFailedException(
                                "Payment Failed"
                        )
                );


        mockMvc.perform(
                        post("/api/v1/payments")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Payment Failed"));

        verify(paymentService)
                .createPayment(any(CreatePaymentRequestDTO.class));
    }


    @Test
    void getPaymentByOrder_OrderNotFound() throws Exception {

        Long orderId = 999L;

        when(paymentService.getPaymentByOrderId(orderId))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order Not Found"
                        )
                );


        mockMvc.perform(
                        get("/api/v1/payments/order/{orderId}", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(paymentService)
                .getPaymentByOrderId(orderId);
    }


    @Test
    void getPaymentByOrder_PaymentNotFound() throws Exception {

        Long orderId = 1L;

        when(paymentService.getPaymentByOrderId(orderId))
                .thenThrow(
                        new PaymentNotFoundException(
                                "Payment Not Found"
                        )
                );


        mockMvc.perform(
                        get("/api/v1/payments/order/{orderId}", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Payment Not Found"));

        verify(paymentService)
                .getPaymentByOrderId(orderId);
    }
}
