package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.response.DashboardResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.entity.Order;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentStatus;
import com.ecom.shopsphere.exception.InvalidOrderStatusException;
import com.ecom.shopsphere.exception.OrderNotFoundException;
import com.ecom.shopsphere.mapper.OrderMapper;
import com.ecom.shopsphere.repository.OrderRepository;
import com.ecom.shopsphere.repository.ProductRepository;
import com.ecom.shopsphere.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Test
    void getAllOrders_Success() {
        Order order1 = Order.builder().orderId(1L).orderNumber("ORD-001").build();
        Order order2 = Order.builder().orderId(2L).orderNumber("ORD-002").build();
        OrderResponseDTO response1 = OrderResponseDTO.builder().orderId(1L).build();
        OrderResponseDTO response2 = OrderResponseDTO.builder().orderId(2L).build();

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));
        when(orderMapper.toResponse(order1)).thenReturn(response1);
        when(orderMapper.toResponse(order2)).thenReturn(response2);

        List<OrderResponseDTO> result = adminService.getAllOrders();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(orderRepository).findAll();
        verify(orderMapper).toResponse(order1);
        verify(orderMapper).toResponse(order2);
    }

    @Test
    void getAllOrders_Empty() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderResponseDTO> result = adminService.getAllOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_Success() {
        Long orderId = 1L;
        Order order = Order.builder().orderId(orderId).orderNumber("ORD-001").build();
        OrderResponseDTO response = OrderResponseDTO.builder().orderId(orderId).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponseDTO result = adminService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());

        verify(orderRepository).findById(orderId);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void getOrderById_NotFound() {
        Long orderId = 99L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> adminService.getOrderById(orderId));

        assertEquals("Order not found.", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderMapper, never()).toResponse(any(Order.class));
    }

    @Test
    void confirmOrder_Success() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PENDING)
                .build();
        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponseDTO result = adminService.confirmOrder(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getOrderStatus());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
    }

    @Test
    void confirmOrder_NotPending_ThrowsException() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderStatusException exception = assertThrows(InvalidOrderStatusException.class,
                () -> adminService.confirmOrder(orderId));

        assertEquals("Only pending orders can be confirmed.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void confirmOrder_NotFound() {
        Long orderId = 99L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> adminService.confirmOrder(orderId));

        assertEquals("Order not found.", exception.getMessage());
    }

    @Test
    void processOrder_Success() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CONFIRMED)
                .build();
        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PROCESSING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponseDTO result = adminService.processOrder(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.getOrderStatus());

        verify(orderRepository).save(order);
        assertEquals(OrderStatus.PROCESSING, order.getOrderStatus());
    }

    @Test
    void processOrder_NotConfirmed_ThrowsException() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderStatusException exception = assertThrows(InvalidOrderStatusException.class,
                () -> adminService.processOrder(orderId));

        assertEquals("Only confirmed orders can be processed.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processOrder_NotFound() {
        Long orderId = 99L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> adminService.processOrder(orderId));

        assertEquals("Order not found.", exception.getMessage());
    }

    @Test
    void shipOrder_Success() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PROCESSING)
                .build();
        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.SHIPPED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponseDTO result = adminService.shipOrder(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPED, result.getOrderStatus());

        verify(orderRepository).save(order);
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());
    }

    @Test
    void shipOrder_NotProcessing_ThrowsException() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderStatusException exception = assertThrows(InvalidOrderStatusException.class,
                () -> adminService.shipOrder(orderId));

        assertEquals("Only processing orders can be shipped.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shipOrder_NotFound() {
        Long orderId = 99L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> adminService.shipOrder(orderId));

        assertEquals("Order not found.", exception.getMessage());
    }

    @Test
    void deliverOrder_Success() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.SHIPPED)
                .build();
        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.DELIVERED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponseDTO result = adminService.deliverOrder(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.DELIVERED, result.getOrderStatus());

        verify(orderRepository).save(order);
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
    }

    @Test
    void deliverOrder_NotShipped_ThrowsException() {
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PROCESSING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderStatusException exception = assertThrows(InvalidOrderStatusException.class,
                () -> adminService.deliverOrder(orderId));

        assertEquals("Only shipped orders can be delivered.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deliverOrder_NotFound() {
        Long orderId = 99L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> adminService.deliverOrder(orderId));

        assertEquals("Order not found.", exception.getMessage());
    }

    @Test
    void getDashboard_Success() {

        // Arrange

        when(userRepository.count()).thenReturn(10L);

        when(productRepository.count()).thenReturn(25L);

        when(orderRepository.count()).thenReturn(20L);


        when(orderRepository.countByOrderStatus(OrderStatus.PENDING)).thenReturn(3L);

        when(orderRepository.countByOrderStatus(OrderStatus.CONFIRMED)).thenReturn(4L);

        when(orderRepository.countByOrderStatus(OrderStatus.PROCESSING)).thenReturn(3L);

        when(orderRepository.countByOrderStatus(OrderStatus.SHIPPED)).thenReturn(5L);

        when(orderRepository.countByOrderStatus(OrderStatus.DELIVERED)).thenReturn(4L);

        when(orderRepository.countByOrderStatus(OrderStatus.CANCELLED)).thenReturn(1L);


        when(orderRepository.calculateTotalRevenue(PaymentStatus.SUCCESS)).thenReturn(new BigDecimal("15000.00"));


        // Act

        DashboardResponseDTO result = adminService.getDashboard();


        // Assert

        assertNotNull(result);

        assertEquals(10L, result.getTotalUsers());

        assertEquals(25L, result.getTotalProducts());

        assertEquals(20L, result.getTotalOrders());

        assertEquals(3L, result.getPendingOrders());

        assertEquals(4L, result.getConfirmedOrders());

        assertEquals(3L, result.getProcessingOrders());

        assertEquals(5L, result.getShippedOrders());

        assertEquals(4L, result.getDeliveredOrders());

        assertEquals(1L, result.getCancelledOrders());

        assertEquals(new BigDecimal("15000.00"), result.getTotalRevenue());


        // Verify

        verify(userRepository).count();

        verify(productRepository).count();

        verify(orderRepository).count();

        verify(orderRepository).countByOrderStatus(OrderStatus.PENDING);

        verify(orderRepository).countByOrderStatus(OrderStatus.CONFIRMED);

        verify(orderRepository).countByOrderStatus(OrderStatus.PROCESSING);

        verify(orderRepository).countByOrderStatus(OrderStatus.SHIPPED);

        verify(orderRepository).countByOrderStatus(OrderStatus.DELIVERED);

        verify(orderRepository).countByOrderStatus(OrderStatus.CANCELLED);

        verify(orderRepository).calculateTotalRevenue(PaymentStatus.SUCCESS);
    }
}
