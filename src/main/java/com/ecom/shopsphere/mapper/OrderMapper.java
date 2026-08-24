package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.response.OrderItemResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.entity.Order;
import com.ecom.shopsphere.entity.OrderItem;

public interface OrderMapper {

    OrderResponseDTO toResponse(Order order);

    OrderItemResponseDTO toItemResponse(OrderItem orderItem);
}