package com.ecom.shopsphere.repository;

import com.ecom.shopsphere.entity.Order;
import com.ecom.shopsphere.entity.OrderStatus;
import com.ecom.shopsphere.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserId(Long userId);

    Optional<Order> findByOrderIdAndUserUserId(
            Long orderId,
            Long userId);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAllByOrderByCreatedAtDesc();

    long countByOrderStatus(OrderStatus orderStatus);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.paymentStatus = :paymentStatus
        """)
    BigDecimal calculateTotalRevenue(
            @Param("paymentStatus") PaymentStatus paymentStatus);
}