package com.ecom.shopsphere.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Health Check",
        description = "API for checking the application's health and availability status."
)
@RestController
@RequestMapping("/api/v1/shopsphere")
public class HealthController {

    @Operation(
            summary = "Health check",
            description = "Returns a simple status message to confirm that the ShopSphere backend application is running and accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application is running")
    })
    @GetMapping("/health")
    public String health() {
        return "ShopSphere Backend Running!";
    }
}
