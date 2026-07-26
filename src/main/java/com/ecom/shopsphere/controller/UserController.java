package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.*;
import com.ecom.shopsphere.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> getProfile() {

        log.info("Received request to fetch user profile.");

        ProfileResponseDTO response =
                userService.getProfile();

        ApiResponse<ProfileResponseDTO> apiResponse =
                ApiResponse.<ProfileResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Profile fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request) {

        log.info("Received request to update profile.");

        ProfileResponseDTO response =
                userService.updateProfile(request);

        ApiResponse<ProfileResponseDTO> apiResponse =
                ApiResponse.<ProfileResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Profile updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<ChangePasswordResponseDTO>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request) {

        log.info("Received request to change password.");

        ChangePasswordResponseDTO response =
                userService.changePassword(request);

        log.info("Password changed successfully for user.");

        ApiResponse<ChangePasswordResponseDTO> apiResponse =
                ApiResponse.<ChangePasswordResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Password changed successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<DeleteAccountResponseDTO>> deleteAccount() {

        log.info("Received request to delete account.");

        DeleteAccountResponseDTO response =
                userService.deleteAccount();

        ApiResponse<DeleteAccountResponseDTO> apiResponse =
                ApiResponse.<DeleteAccountResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Account deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);
    }
}