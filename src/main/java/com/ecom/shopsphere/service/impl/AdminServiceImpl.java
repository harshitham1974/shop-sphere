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
import com.ecom.shopsphere.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    @Override
    public List<OrderResponseDTO> getAllOrders() {

        log.info("Fetching all orders.");

        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) {

        log.info("Fetching order ID: {}", orderId);

        Order order = findOrder(orderId);

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponseDTO confirmOrder(Long orderId) {

        log.info("Confirming order ID: {}", orderId);

        Order order = findOrder(orderId);

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Only pending orders can be confirmed.");
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponseDTO processOrder(Long orderId) {

        log.info("Processing order ID: {}", orderId);

        Order order = findOrder(orderId);

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStatusException("Only confirmed orders can be processed.");
        }

        order.setOrderStatus(OrderStatus.PROCESSING);

        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponseDTO shipOrder(Long orderId) {

        log.info("Shipping order ID: {}", orderId);

        Order order = findOrder(orderId);

        if (order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStatusException("Only processing orders can be shipped.");
        }

        order.setOrderStatus(OrderStatus.SHIPPED);

        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponseDTO deliverOrder(Long orderId) {

        log.info("Delivering order ID: {}", orderId);

        Order order = findOrder(orderId);

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStatusException("Only shipped orders can be delivered.");
        }

        order.setOrderStatus(OrderStatus.DELIVERED);

        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    private Order findOrder(Long orderId) {

        return orderRepository.findById(orderId).orElseThrow(() -> {

            log.error("Order not found: {}", orderId);

            return new OrderNotFoundException("Order not found.");
        });
    }

    @Override
    public DashboardResponseDTO getDashboard() {

        log.info("Fetching admin dashboard.");

        long totalUsers = userRepository.count();

        long totalProducts = productRepository.count();

        long totalOrders = orderRepository.count();

        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);

        long confirmedOrders = orderRepository.countByOrderStatus(OrderStatus.CONFIRMED);

        long processingOrders = orderRepository.countByOrderStatus(OrderStatus.PROCESSING);

        long shippedOrders = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);

        long deliveredOrders = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);

        long cancelledOrders = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(PaymentStatus.SUCCESS);


        return DashboardResponseDTO.builder()

                .totalUsers(totalUsers)

                .totalProducts(totalProducts)

                .totalOrders(totalOrders)

                .pendingOrders(pendingOrders)

                .confirmedOrders(confirmedOrders)

                .processingOrders(processingOrders)

                .shippedOrders(shippedOrders)

                .deliveredOrders(deliveredOrders)

                .cancelledOrders(cancelledOrders)

                .totalRevenue(totalRevenue)

                .build();
    }
}