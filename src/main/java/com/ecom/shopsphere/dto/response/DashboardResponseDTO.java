package com.ecom.shopsphere.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {

    private Long totalUsers;

    private Long totalProducts;

    private Long totalOrders;

    private Long pendingOrders;

    private Long confirmedOrders;

    private Long processingOrders;

    private Long shippedOrders;

    private Long deliveredOrders;

    private Long cancelledOrders;

    private BigDecimal totalRevenue;

}