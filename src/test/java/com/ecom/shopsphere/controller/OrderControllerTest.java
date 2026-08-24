package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateOrderRequestDTO;
import com.ecom.shopsphere.dto.response.CancelOrderResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentStatus;
import com.ecom.shopsphere.exception.AddressNotFoundException;
import com.ecom.shopsphere.exception.CartEmptyException;
import com.ecom.shopsphere.exception.OrderCancellationFailedException;
import com.ecom.shopsphere.exception.OrderNotFoundException;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.OrderService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void createOrder_Success() throws Exception {

        CreateOrderRequestDTO request =
                CreateOrderRequestDTO.builder()
                        .shippingAddressId(1L)
                        .build();

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-123456")
                        .totalAmount(new BigDecimal("99.99"))
                        .orderStatus(OrderStatus.PROCESSING)
                        .paymentStatus(PaymentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(orderService.createOrder(any(CreateOrderRequestDTO.class)))
                .thenReturn(response);


        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Order placed successfully."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderNumber")
                        .value("ORD-123456"))
                .andExpect(jsonPath("$.data.totalAmount")
                        .value(99.99))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("PROCESSING"))
                .andExpect(jsonPath("$.data.paymentStatus")
                        .value("PENDING"));

        verify(orderService)
                .createOrder(any(CreateOrderRequestDTO.class));
    }


    @Test
    void getMyOrders_Success() throws Exception {

        OrderResponseDTO order1 =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("49.99"))
                        .orderStatus(OrderStatus.CONFIRMED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build();

        OrderResponseDTO order2 =
                OrderResponseDTO.builder()
                        .orderId(2L)
                        .orderNumber("ORD-002")
                        .totalAmount(new BigDecimal("149.99"))
                        .orderStatus(OrderStatus.SHIPPED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(orderService.getMyOrders())
                .thenReturn(List.of(order1, order2));


        mockMvc.perform(
                        get("/api/v1/orders")
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Orders fetched successfully."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].orderId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].orderNumber")
                        .value("ORD-001"))
                .andExpect(jsonPath("$.data[1].orderId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].orderNumber")
                        .value("ORD-002"));

        verify(orderService)
                .getMyOrders();
    }


    @Test
    void getOrderById_Success() throws Exception {

        Long orderId = 1L;

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(orderId)
                        .orderNumber("ORD-123456")
                        .totalAmount(new BigDecimal("99.99"))
                        .orderStatus(OrderStatus.CONFIRMED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(orderService.getOrderById(orderId))
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/v1/orders/{orderId}", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order fetched successfully."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderNumber")
                        .value("ORD-123456"))
                .andExpect(jsonPath("$.data.totalAmount")
                        .value(99.99))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("CONFIRMED"))
                .andExpect(jsonPath("$.data.paymentStatus")
                        .value("SUCCESS"));

        verify(orderService)
                .getOrderById(orderId);
    }


    @Test
    void cancelOrder_Success() throws Exception {

        Long orderId = 1L;

        CancelOrderResponseDTO response =
                CancelOrderResponseDTO.builder()
                        .state("CANCELLED")
                        .build();

        when(orderService.cancelOrder(orderId))
                .thenReturn(response);


        mockMvc.perform(
                        patch("/api/v1/orders/{orderId}/cancel", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order cancelled successfully."))
                .andExpect(jsonPath("$.data.state")
                        .value("CANCELLED"));

        verify(orderService)
                .cancelOrder(orderId);
    }


    @Test
    void createOrder_CartEmpty() throws Exception {

        CreateOrderRequestDTO request =
                CreateOrderRequestDTO.builder()
                        .shippingAddressId(1L)
                        .build();

        when(orderService.createOrder(any(CreateOrderRequestDTO.class)))
                .thenThrow(
                        new CartEmptyException(
                                "Cart Empty"
                        )
                );


        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Cart Empty"));

        verify(orderService)
                .createOrder(any(CreateOrderRequestDTO.class));
    }


    @Test
    void createOrder_AddressNotFound() throws Exception {

        CreateOrderRequestDTO request =
                CreateOrderRequestDTO.builder()
                        .shippingAddressId(99L)
                        .build();

        when(orderService.createOrder(any(CreateOrderRequestDTO.class)))
                .thenThrow(
                        new AddressNotFoundException(
                                "Address not found"
                        )
                );


        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Address Not Found"));

        verify(orderService)
                .createOrder(any(CreateOrderRequestDTO.class));
    }


    @Test
    void getOrderById_OrderNotFound() throws Exception {

        Long orderId = 999L;

        when(orderService.getOrderById(orderId))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );


        mockMvc.perform(
                        get("/api/v1/orders/{orderId}", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(orderService)
                .getOrderById(orderId);
    }


    @Test
    void cancelOrder_OrderNotFound() throws Exception {

        Long orderId = 999L;

        when(orderService.cancelOrder(orderId))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );


        mockMvc.perform(
                        patch("/api/v1/orders/{orderId}/cancel", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(orderService)
                .cancelOrder(orderId);
    }


    @Test
    void cancelOrder_CancellationFailed() throws Exception {

        Long orderId = 1L;

        when(orderService.cancelOrder(orderId))
                .thenThrow(
                        new OrderCancellationFailedException(
                                "Order Cancellation Failed"
                        )
                );


        mockMvc.perform(
                        patch("/api/v1/orders/{orderId}/cancel", orderId)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Order Cancellation Failed"));

        verify(orderService)
                .cancelOrder(orderId);
    }
}
