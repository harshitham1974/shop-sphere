package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.DashboardResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Admin Order Management",
        description = "Administrative APIs for managing all customer orders, including viewing, confirming, processing, shipping, and delivering orders."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Get admin dashboard",
            description = "Retrieves dashboard statistics including total orders, total revenue, and order status counts. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard data fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDTO<DashboardResponseDTO>> getDashboard() {

        log.info("Received request to fetch admin dashboard.");

        DashboardResponseDTO response =
                adminService.getDashboard();

        ApiResponseDTO<DashboardResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DashboardResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Dashboard fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get all orders",
            description = "Retrieves a list of all orders placed in the system. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getAllOrders() {

        log.info("Received request to fetch all orders.");

        List<OrderResponseDTO> response =
                adminService.getAllOrders();

        ApiResponseDTO<List<OrderResponseDTO>> apiResponseDTO =
                ApiResponseDTO.<List<OrderResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Orders fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a specific order by its ID with full details. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderById(
            @PathVariable Long orderId) {

        log.info(
                "Received request to fetch order ID: {}",
                orderId);

        OrderResponseDTO response =
                adminService.getOrderById(orderId);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO =
                ApiResponseDTO.<OrderResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Order fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Confirm order",
            description = "Marks an order as CONFIRMED, indicating the order has been received and accepted. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> confirmOrder(
            @PathVariable Long orderId) {

        log.info(
                "Received request to confirm order ID: {}",
                orderId);

        OrderResponseDTO response =
                adminService.confirmOrder(orderId);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO =
                ApiResponseDTO.<OrderResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Order confirmed successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Process order",
            description = "Marks an order as PROCESSING, indicating the order is being prepared. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order moved to processing successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/processing")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> processOrder(
            @PathVariable Long orderId) {

        log.info(
                "Received request to process order ID: {}",
                orderId);

        OrderResponseDTO response =
                adminService.processOrder(orderId);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO =
                ApiResponseDTO.<OrderResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Order moved to processing.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Ship order",
            description = "Marks an order as SHIPPED, indicating the order has been dispatched. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order shipped successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> shipOrder(
            @PathVariable Long orderId) {

        log.info(
                "Received request to ship order ID: {}",
                orderId);

        OrderResponseDTO response =
                adminService.shipOrder(orderId);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO =
                ApiResponseDTO.<OrderResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Order shipped successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Deliver order",
            description = "Marks an order as DELIVERED, indicating the order has been successfully delivered to the customer. Requires admin authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order delivered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not admin"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> deliverOrder(
            @PathVariable Long orderId) {

        log.info(
                "Received request to deliver order ID: {}",
                orderId);

        OrderResponseDTO response =
                adminService.deliverOrder(orderId);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO =
                ApiResponseDTO.<OrderResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Order delivered successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
