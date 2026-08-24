package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Auth Management",
        description = "APIs for user registration and authentication."
)
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<RegisterResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        log.info("Received registration request for email: {}", request.getEmail());

        RegisterResponseDTO response = authService.registerUser(request);

        log.info("Registration request completed successfully for email: {}", request.getEmail());

        ApiResponseDTO<RegisterResponseDTO> apiResponseDTO =
                ApiResponseDTO.<RegisterResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Registration successful")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "User login",
            description = "Authenticates the user and returns a JWT token."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        log.info("Received login request for email: {}", request.getEmail());

        LoginResponseDTO response = authService.loginUser(request);

        ApiResponseDTO<LoginResponseDTO> apiResponseDTO =
                ApiResponseDTO.<LoginResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Login successful")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

}
