package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.*;
import com.ecom.shopsphere.service.UserService;
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
        name = "User Management",
        description = "APIs for user profile management, including fetching profile, updating profile, changing password, and deleting account."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get Profile",
            description = "Fetches the currently logged-in user's profile information. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO<ProfileResponseDTO>> getProfile() {

        log.info("Received request to fetch user profile.");

        ProfileResponseDTO response =
                userService.getProfile();

        ApiResponseDTO<ProfileResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ProfileResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Profile fetched successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Update Profile",
            description = "Updates the currently logged-in user's profile information including name, phone number, etc. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponseDTO<ProfileResponseDTO>> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request) {

        log.info("Received request to update profile.");

        ProfileResponseDTO response =
                userService.updateProfile(request);

        ApiResponseDTO<ProfileResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ProfileResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Profile updated successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Change Password",
            description = "Changes the currently logged-in user's password. Requires the current password and the new password. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or current password incorrect"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponseDTO<ChangePasswordResponseDTO>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request) {

        log.info("Received request to change password.");

        ChangePasswordResponseDTO response =
                userService.changePassword(request);

        log.info("Password changed successfully for user.");

        ApiResponseDTO<ChangePasswordResponseDTO> apiResponseDTO =
                ApiResponseDTO.<ChangePasswordResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Password changed successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Delete Account",
            description = "Permanently deletes the currently logged-in user's account and all associated data. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponseDTO<DeleteAccountResponseDTO>> deleteAccount() {

        log.info("Received request to delete account.");

        DeleteAccountResponseDTO response =
                userService.deleteAccount();

        ApiResponseDTO<DeleteAccountResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteAccountResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Account deleted successfully")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
