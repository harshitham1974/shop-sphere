package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.CreateOrderRequestDTO;
import com.ecom.shopsphere.dto.response.CancelOrderResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(
            CreateOrderRequestDTO request);


    List<OrderResponseDTO> getMyOrders();


    OrderResponseDTO getOrderById(
            Long orderId);


    CancelOrderResponseDTO cancelOrder(
            Long orderId);
}