package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.response.DashboardResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;

import java.util.List;

public interface AdminService {

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO getOrderById(Long orderId);

    OrderResponseDTO confirmOrder(Long orderId);

    OrderResponseDTO processOrder(Long orderId);

    OrderResponseDTO shipOrder(Long orderId);

    OrderResponseDTO deliverOrder(Long orderId);

    DashboardResponseDTO getDashboard();

}