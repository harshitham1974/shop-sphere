package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.response.DashboardResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentStatus;
import com.ecom.shopsphere.exception.InvalidOrderStatusException;
import com.ecom.shopsphere.exception.OrderNotFoundException;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.AdminService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void getDashboard_Success() throws Exception {

        DashboardResponseDTO response =
                DashboardResponseDTO.builder()
                        .totalUsers(100L)
                        .totalProducts(50L)
                        .totalOrders(200L)
                        .pendingOrders(10L)
                        .confirmedOrders(20L)
                        .processingOrders(30L)
                        .shippedOrders(40L)
                        .deliveredOrders(95L)
                        .cancelledOrders(5L)
                        .totalRevenue(new BigDecimal("99999.99"))
                        .build();

        when(adminService.getDashboard())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/admin/orders/dashboard")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Dashboard fetched successfully."))
                .andExpect(jsonPath("$.data.totalUsers")
                        .value(100))
                .andExpect(jsonPath("$.data.totalProducts")
                        .value(50))
                .andExpect(jsonPath("$.data.totalOrders")
                        .value(200))
                .andExpect(jsonPath("$.data.pendingOrders")
                        .value(10))
                .andExpect(jsonPath("$.data.confirmedOrders")
                        .value(20))
                .andExpect(jsonPath("$.data.processingOrders")
                        .value(30))
                .andExpect(jsonPath("$.data.shippedOrders")
                        .value(40))
                .andExpect(jsonPath("$.data.deliveredOrders")
                        .value(95))
                .andExpect(jsonPath("$.data.cancelledOrders")
                        .value(5))
                .andExpect(jsonPath("$.data.totalRevenue")
                        .value(99999.99));

        verify(adminService)
                .getDashboard();
    }

    @Test
    void getAllOrders_Success() throws Exception {

        OrderResponseDTO order1 =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("299.99"))
                        .orderStatus(OrderStatus.PENDING)
                        .paymentStatus(PaymentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        OrderResponseDTO order2 =
                OrderResponseDTO.builder()
                        .orderId(2L)
                        .orderNumber("ORD-002")
                        .totalAmount(new BigDecimal("499.99"))
                        .orderStatus(OrderStatus.CONFIRMED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        List<OrderResponseDTO> response = List.of(order1, order2);

        when(adminService.getAllOrders())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/admin/orders")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Orders fetched successfully."))
                .andExpect(jsonPath("$.data[0].orderId")
                        .value(1))
                .andExpect(jsonPath("$.data[0].orderNumber")
                        .value("ORD-001"))
                .andExpect(jsonPath("$.data[0].orderStatus")
                        .value("PENDING"))
                .andExpect(jsonPath("$.data[1].orderId")
                        .value(2))
                .andExpect(jsonPath("$.data[1].orderNumber")
                        .value("ORD-002"))
                .andExpect(jsonPath("$.data[1].orderStatus")
                        .value("CONFIRMED"));

        verify(adminService)
                .getAllOrders();
    }

    @Test
    void getOrderById_Success() throws Exception {

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("299.99"))
                        .orderStatus(OrderStatus.PENDING)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        when(adminService.getOrderById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/admin/orders/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order fetched successfully."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderNumber")
                        .value("ORD-001"))
                .andExpect(jsonPath("$.data.totalAmount")
                        .value(299.99))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("PENDING"))
                .andExpect(jsonPath("$.data.paymentStatus")
                        .value("SUCCESS"));

        verify(adminService)
                .getOrderById(1L);
    }

    @Test
    void confirmOrder_Success() throws Exception {

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("299.99"))
                        .orderStatus(OrderStatus.CONFIRMED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        when(adminService.confirmOrder(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/confirm")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order confirmed successfully."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("CONFIRMED"));

        verify(adminService)
                .confirmOrder(1L);
    }

    @Test
    void processOrder_Success() throws Exception {

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("299.99"))
                        .orderStatus(OrderStatus.PROCESSING)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        when(adminService.processOrder(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/processing")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order moved to processing."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("PROCESSING"));

        verify(adminService)
                .processOrder(1L);
    }

    @Test
    void shipOrder_Success() throws Exception {

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("299.99"))
                        .orderStatus(OrderStatus.SHIPPED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        when(adminService.shipOrder(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/ship")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order shipped successfully."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("SHIPPED"));

        verify(adminService)
                .shipOrder(1L);
    }

    @Test
    void deliverOrder_Success() throws Exception {

        OrderResponseDTO response =
                OrderResponseDTO.builder()
                        .orderId(1L)
                        .orderNumber("ORD-001")
                        .totalAmount(new BigDecimal("299.99"))
                        .orderStatus(OrderStatus.DELIVERED)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .orderItems(List.of())
                        .build();

        when(adminService.deliverOrder(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/deliver")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Order delivered successfully."))
                .andExpect(jsonPath("$.data.orderId")
                        .value(1))
                .andExpect(jsonPath("$.data.orderStatus")
                        .value("DELIVERED"));

        verify(adminService)
                .deliverOrder(1L);
    }

    @Test
    void getOrderById_OrderNotFoundException() throws Exception {

        when(adminService.getOrderById(999L))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/admin/orders/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(adminService)
                .getOrderById(999L);
    }

    @Test
    void confirmOrder_OrderNotFoundException() throws Exception {

        when(adminService.confirmOrder(999L))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/admin/orders/999/confirm")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(adminService)
                .confirmOrder(999L);
    }

    @Test
    void confirmOrder_InvalidOrderStatusException() throws Exception {

        when(adminService.confirmOrder(1L))
                .thenThrow(
                        new InvalidOrderStatusException(
                                "Invalid order status transition"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/confirm")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid Order Status"));

        verify(adminService)
                .confirmOrder(1L);
    }

    @Test
    void processOrder_InvalidOrderStatusException() throws Exception {

        when(adminService.processOrder(1L))
                .thenThrow(
                        new InvalidOrderStatusException(
                                "Invalid order status transition"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/processing")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid Order Status"));

        verify(adminService)
                .processOrder(1L);
    }

    @Test
    void shipOrder_OrderNotFoundException() throws Exception {

        when(adminService.shipOrder(999L))
                .thenThrow(
                        new OrderNotFoundException(
                                "Order not found"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/admin/orders/999/ship")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Order Not Found"));

        verify(adminService)
                .shipOrder(999L);
    }

    @Test
    void deliverOrder_InvalidOrderStatusException() throws Exception {

        when(adminService.deliverOrder(1L))
                .thenThrow(
                        new InvalidOrderStatusException(
                                "Invalid order status transition"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/admin/orders/1/deliver")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid Order Status"));

        verify(adminService)
                .deliverOrder(1L);
    }
}
