package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.AddAddressRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateAddressRequestDTO;
import com.ecom.shopsphere.dto.response.AddressResponseDTO;
import com.ecom.shopsphere.dto.response.ApiResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteAddressResponseDTO;
import com.ecom.shopsphere.service.AddressService;
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
        name = "Address Management",
        description = "APIs for managing user addresses including adding, retrieving, updating, deleting, and setting default addresses."
)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(
            summary = "Add address",
            description = "Adds a new delivery address for the current user. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address added successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> addAddress(
            @Valid @RequestBody AddAddressRequestDTO request) {

        log.info("Received request to add address.");

        AddressResponseDTO response =
                addressService.addAddress(request);

        ApiResponseDTO<AddressResponseDTO> apiResponseDTO =
                ApiResponseDTO.<AddressResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Address added successfully.")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponseDTO);
    }

    @Operation(
            summary = "Get all addresses",
            description = "Retrieves all addresses belonging to the current user. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addresses fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AddressResponseDTO>>> getAllAddresses() {

        log.info("Received request to fetch all addresses.");

        List<AddressResponseDTO> response =
                addressService.getAllAddresses();

        ApiResponseDTO<List<AddressResponseDTO>> apiResponseDTO =
                ApiResponseDTO.<List<AddressResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Addresses fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Get address by ID",
            description = "Retrieves a specific address by its ID. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> getAddressById(
            @PathVariable Long addressId) {

        log.info(
                "Received request to fetch address ID: {}",
                addressId);

        AddressResponseDTO response =
                addressService.getAddressById(addressId);

        ApiResponseDTO<AddressResponseDTO> apiResponseDTO =
                ApiResponseDTO.<AddressResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Address fetched successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Update address",
            description = "Updates an existing address. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequestDTO request) {

        log.info(
                "Received request to update address ID: {}",
                addressId);

        AddressResponseDTO response =
                addressService.updateAddress(
                        addressId,
                        request);

        ApiResponseDTO<AddressResponseDTO> apiResponseDTO =
                ApiResponseDTO.<AddressResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Address updated successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Delete address",
            description = "Deletes an existing address. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponseDTO<DeleteAddressResponseDTO>> deleteAddress(
            @PathVariable Long addressId) {

        log.info(
                "Received request to delete address ID: {}",
                addressId);

        DeleteAddressResponseDTO responseDTO = addressService.deleteAddress(addressId);

        ApiResponseDTO<DeleteAddressResponseDTO> apiResponseDTO =
                ApiResponseDTO.<DeleteAddressResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Address deleted successfully.")
                        .data(responseDTO)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }

    @Operation(
            summary = "Set default address",
            description = "Marks a specific address as the default delivery address. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Default address updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> setDefaultAddress(
            @PathVariable Long addressId) {

        log.info(
                "Received request to set default address ID: {}",
                addressId);

        AddressResponseDTO response =
                addressService.setDefaultAddress(addressId);

        ApiResponseDTO<AddressResponseDTO> apiResponseDTO =
                ApiResponseDTO.<AddressResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Default address updated successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponseDTO);
    }
}
