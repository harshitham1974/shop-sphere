package com.ecom.shopsphere.repository;

import com.ecom.shopsphere.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

}