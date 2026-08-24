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
import com.ecom.shopsphere.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final CartRepository cartRepository;

    private final AddressRepository addressRepository;

    private final ProductRepository productRepository;

    private final CurrentUserService currentUserService;

    private final OrderMapper orderMapper;


    @Override
    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {

        log.info("Creating new order.");

        User user = currentUserService.getCurrentUser();

        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> {
                    log.error("Cart not found for user.");
                    return new CartEmptyException(
                            "Cart is empty. Cannot place order. Add products before placing an order."
                    );
                });

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.error("Cart is empty. Cannot create order.");
            throw new CartEmptyException(
                    "Cart is empty. Cannot place order. Add products before placing an order."
            );
        }

        Address address = addressRepository.findByAddressIdAndUserUserId(request.getShippingAddressId(), user.getUserId()).orElseThrow(() -> {
            log.error("Address not found.");
            return new AddressNotFoundException("Address not found.");
        });

        Order order = Order.builder().orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8)).orderStatus(OrderStatus.PENDING).paymentStatus(PaymentStatus.PENDING).user(user).shippingAddress(address).totalAmount(BigDecimal.ZERO).orderItems(new ArrayList<>()).build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder().product(product).quantity(cartItem.getQuantity()).priceAtPurchase(product.getPrice()).order(order).build();

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(subtotal);

            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();

        cartRepository.save(cart);

        log.info("Order created successfully. ID: {}", savedOrder.getOrderId());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public List<OrderResponseDTO> getMyOrders() {

        log.info("Fetching orders for logged-in user.");

        User user = currentUserService.getCurrentUser();

        List<Order> orders = orderRepository.findByUserUserId(user.getUserId());

        log.info("{} orders found for user: {}", orders.size(), user.getEmail());

        return orders.stream().map(orderMapper::toResponse).toList();
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) {

        log.info("Fetching order ID: {}", orderId);

        User user = currentUserService.getCurrentUser();

        Order order = orderRepository.findByOrderIdAndUserUserId(orderId, user.getUserId()).orElseThrow(() -> {

            log.error("Order not found. ID: {}", orderId);

            throw new OrderNotFoundException("Order not found.");
        });

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public CancelOrderResponseDTO cancelOrder(Long orderId) {

        log.info("Cancelling order ID: {}", orderId);

        User user = currentUserService.getCurrentUser();

        Order order = orderRepository
                .findByOrderIdAndUserUserId(orderId, user.getUserId())
                .orElseThrow(() -> {
                    log.error("Order not found. ID: {}", orderId);
                    return new OrderNotFoundException("Order not found.");
                });

        if (order.getOrderStatus() == OrderStatus.SHIPPED ||
                order.getOrderStatus() == OrderStatus.DELIVERED) {

            throw new OrderCancellationFailedException(
                    "Order cannot be cancelled at this stage."
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        log.info("Order cancelled successfully.");

        return CancelOrderResponseDTO.builder()
                .state("CANCELLED")
                .build();
    }
}