package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.CreatePaymentRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.PaymentResponseDTO;
import com.ecom.shopsphere.service.PaymentService;
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

@Tag(
        name = "Payment Management",
        description = "APIs for processing payments for orders and retrieving payment details."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create payment",
            description = "Processes a payment for a specific order. Validates the order and updates the payment status upon completion. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment completed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or payment processing error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> createPayment(
            @Valid @RequestBody CreatePaymentRequestDTO request) {

        log.info(
                "Received payment request for order ID: {}",
                request.getOrderId()
        );

        PaymentResponseDTO response =
                paymentService.createPayment(request);

        ApiResponseDTO<PaymentResponseDTO> apiResponseDTO =
                ApiResponseDTO.<PaymentResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Payment completed successfully.")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "Get payment by order",
            description = "Retrieves the payment details for a specific order. Users can only view payments for their own orders. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Payment or order not found")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentByOrder(
            @PathVariable Long orderId) {

        log.info(
                "Fetching payment for order ID: {}",
                orderId
        );

        PaymentResponseDTO response =
                paymentService.getPaymentByOrderId(orderId);

        ApiResponseDTO<PaymentResponseDTO> apiResponseDTO =
                ApiResponseDTO.<PaymentResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Payment fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
