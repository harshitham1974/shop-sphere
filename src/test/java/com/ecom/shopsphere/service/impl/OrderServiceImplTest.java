package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.CreateOrderRequestDTO;
import com.ecom.shopsphere.dto.response.CancelOrderResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.entity.*;
import com.ecom.shopsphere.exception.AddressNotFoundException;
import com.ecom.shopsphere.exception.CartEmptyException;
import com.ecom.shopsphere.exception.OrderCancellationFailedException;
import com.ecom.shopsphere.exception.OrderNotFoundException;
import com.ecom.shopsphere.mapper.OrderMapper;
import com.ecom.shopsphere.repository.*;
import com.ecom.shopsphere.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private OrderMapper orderMapper;

    @Test
    void createOrder_Success() {
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shippingAddressId(1L)
                .build();

        User user = User.builder().userId(1L).email("test@gmail.com").build();

        Product product1 = Product.builder().productId(1L).productName("Laptop").price(new BigDecimal("50000.00")).build();
        Product product2 = Product.builder().productId(2L).productName("Mouse").price(new BigDecimal("500.00")).build();

        CartItem cartItem1 = CartItem.builder().cartItemId(1L).product(product1).quantity(1).build();
        CartItem cartItem2 = CartItem.builder().cartItemId(2L).product(product2).quantity(2).build();

        Cart cart = Cart.builder()
                .cartId(1L)
                .user(user)
                .cartItems(new ArrayList<>(List.of(cartItem1, cartItem2)))
                .build();

        Address address = Address.builder().addressId(1L).fullName("Test").build();

        Order savedOrder = Order.builder()
                .orderId(1L)
                .orderNumber("ORD-ABC12345")
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(new BigDecimal("51000.00"))
                .build();

        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(1L)
                .orderNumber("ORD-ABC12345")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByAddressIdAndUserUserId(1L, 1L)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(response);

        OrderResponseDTO result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());

        verify(cartRepository).findByUserUserId(1L);
        verify(addressRepository).findByAddressIdAndUserUserId(1L, 1L);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
        assertTrue(cart.getCartItems().isEmpty());
    }

    @Test
    void createOrder_CartNotFound_ThrowsException() {
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shippingAddressId(1L)
                .build();

        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        CartEmptyException exception = assertThrows(CartEmptyException.class,
                () -> orderService.createOrder(request));

        assertEquals("Cart is empty. Cannot place order. Add products before placing an order.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_AddressNotFound_ThrowsException() {
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .shippingAddressId(99L)
                .build();

        User user = User.builder().userId(1L).build();
        Cart cart = Cart.builder().cartId(1L).cartItems(new ArrayList<>(List.of(CartItem.builder().build()))).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByAddressIdAndUserUserId(99L, 1L)).thenReturn(Optional.empty());

        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class,
                () -> orderService.createOrder(request));

        assertEquals("Address not found.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getMyOrders_Success() {
        User user = User.builder().userId(1L).build();
        Order order1 = Order.builder().orderId(1L).orderNumber("ORD-001").build();
        Order order2 = Order.builder().orderId(2L).orderNumber("ORD-002").build();
        OrderResponseDTO response1 = OrderResponseDTO.builder().orderId(1L).build();
        OrderResponseDTO response2 = OrderResponseDTO.builder().orderId(2L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserUserId(1L)).thenReturn(List.of(order1, order2));
        when(orderMapper.toResponse(order1)).thenReturn(response1);
        when(orderMapper.toResponse(order2)).thenReturn(response2);

        List<OrderResponseDTO> result = orderService.getMyOrders();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(currentUserService).getCurrentUser();
        verify(orderRepository).findByUserUserId(1L);
    }

    @Test
    void getMyOrders_Empty() {
        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserUserId(1L)).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getMyOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrderById_Success() {
        Long orderId = 1L;
        User user = User.builder().userId(1L).build();
        Order order = Order.builder().orderId(orderId).orderNumber("ORD-001").build();
        OrderResponseDTO response = OrderResponseDTO.builder().orderId(orderId).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponseDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());

        verify(orderRepository).findByOrderIdAndUserUserId(orderId, 1L);
    }

    @Test
    void getOrderById_NotFound() {
        Long orderId = 99L;
        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(orderId));

        assertEquals("Order not found.", exception.getMessage());
        verify(orderMapper, never()).toResponse(any(Order.class));
    }

    @Test
    void cancelOrder_Success_FromPending() {
        Long orderId = 1L;
        User user = User.builder().userId(1L).build();
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PENDING)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.of(order));

        CancelOrderResponseDTO result = orderService.cancelOrder(orderId);

        assertNotNull(result);
        assertEquals("CANCELLED", result.getState());

        verify(orderRepository).save(order);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    void cancelOrder_Success_FromProcessing() {
        Long orderId = 1L;
        User user = User.builder().userId(1L).build();
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PROCESSING)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.of(order));

        CancelOrderResponseDTO result = orderService.cancelOrder(orderId);

        assertNotNull(result);
        assertEquals("CANCELLED", result.getState());
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    void cancelOrder_Failed_Shipped_ThrowsException() {
        Long orderId = 1L;
        User user = User.builder().userId(1L).build();
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.SHIPPED)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.of(order));

        OrderCancellationFailedException exception = assertThrows(OrderCancellationFailedException.class,
                () -> orderService.cancelOrder(orderId));

        assertEquals("Order cannot be cancelled at this stage.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_Failed_Delivered_ThrowsException() {
        Long orderId = 1L;
        User user = User.builder().userId(1L).build();
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.DELIVERED)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.of(order));

        OrderCancellationFailedException exception = assertThrows(OrderCancellationFailedException.class,
                () -> orderService.cancelOrder(orderId));

        assertEquals("Order cannot be cancelled at this stage.", exception.getMessage());
    }

    @Test
    void cancelOrder_NotFound_ThrowsRuntimeException() {
        Long orderId = 99L;
        User user = User.builder().userId(1L).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderIdAndUserUserId(orderId, 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(orderId));

        assertEquals("Order not found.", exception.getMessage());
    }
}
