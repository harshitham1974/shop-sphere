package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.response.ApiResponse;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        log.info("Received registration request for email: {}", request.getEmail());

        RegisterResponseDTO response = userService.registerUser(request);

        log.info("Registration request completed successfully for email: {}", request.getEmail());

        ApiResponse<RegisterResponseDTO> apiResponse =
                ApiResponse.<RegisterResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Registration successful")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        log.info("Received login request for email: {}", request.getEmail());

        LoginResponseDTO response = userService.loginUser(request);

        ApiResponse<LoginResponseDTO> apiResponse =
                ApiResponse.<LoginResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Login successful")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }
}