package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.dto.response.OrderItemResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.entity.Order;
import com.ecom.shopsphere.entity.OrderItem;
import com.ecom.shopsphere.mapper.AddressMapper;
import com.ecom.shopsphere.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class OrderMapperImpl implements OrderMapper {


    private final AddressMapper addressMapper;


    @Override
    public OrderResponseDTO toResponse(Order order) {

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())

                .shippingAddress(
                        addressMapper.toResponse(
                                order.getShippingAddress()
                        )
                )

                .orderItems(
                        order.getOrderItems()
                                .stream()
                                .map(this::toItemResponse)
                                .collect(Collectors.toList())
                )

                .build();
    }


    @Override
    public OrderItemResponseDTO toItemResponse(
            OrderItem orderItem) {


        BigDecimal subtotal =
                orderItem.getPriceAtPurchase()
                        .multiply(
                                BigDecimal.valueOf(
                                        orderItem.getQuantity()
                                )
                        );


        return OrderItemResponseDTO.builder()
                .orderItemId(
                        orderItem.getOrderItemId()
                )

                .productId(
                        orderItem.getProduct()
                                .getProductId()
                )

                .productName(
                        orderItem.getProduct()
                                .getProductName()
                )

                .quantity(
                        orderItem.getQuantity()
                )

                .priceAtPurchase(
                        orderItem.getPriceAtPurchase()
                )

                .subtotal(subtotal)

                .build();
    }
}