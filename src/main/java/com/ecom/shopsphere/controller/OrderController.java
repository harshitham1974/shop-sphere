package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreateOrderRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.CancelOrderResponseDTO;
import com.ecom.shopsphere.dto.response.OrderResponseDTO;
import com.ecom.shopsphere.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Order Management",
        description = "APIs for managing customer orders including creating, retrieving, and canceling orders."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create order",
            description = "Creates a new order from the user's current cart contents. Requires a valid address ID and payment method. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or cart is empty"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {

        log.info("Received request to create order.");

        OrderResponseDTO response = orderService.createOrder(request);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO = ApiResponseDTO.<OrderResponseDTO>builder().status(HttpStatus.CREATED.value()).message("Order placed successfully.").data(response).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDTO);
    }

    @Operation(
            summary = "Get my orders",
            description = "Retrieves all orders placed by the currently authenticated user. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getMyOrders() {

        log.info("Received request to fetch orders.");

        List<OrderResponseDTO> response = orderService.getMyOrders();

        ApiResponseDTO<List<OrderResponseDTO>> apiResponseDTO = ApiResponseDTO.<List<OrderResponseDTO>>builder().status(HttpStatus.OK.value()).message("Orders fetched successfully.").data(response).build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a specific order by its ID. Users can only view their own orders. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderById(@PathVariable Long orderId) {

        log.info("Received request to fetch order ID: {}", orderId);

        OrderResponseDTO response = orderService.getOrderById(orderId);

        ApiResponseDTO<OrderResponseDTO> apiResponseDTO = ApiResponseDTO.<OrderResponseDTO>builder().status(HttpStatus.OK.value()).message("Order fetched successfully.").data(response).build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancels a pending or confirmed order. Orders that have already been shipped or delivered cannot be canceled. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cancellation failed - invalid order status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponseDTO<CancelOrderResponseDTO>> cancelOrder(@PathVariable Long orderId) {

        log.info("Received request to cancel order ID: {}", orderId);

        CancelOrderResponseDTO responseDTO = orderService.cancelOrder(orderId);

        ApiResponseDTO<CancelOrderResponseDTO> apiResponseDTO = ApiResponseDTO.<CancelOrderResponseDTO>builder().status(HttpStatus.OK.value()).message("Order cancelled successfully.").data(responseDTO).build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
