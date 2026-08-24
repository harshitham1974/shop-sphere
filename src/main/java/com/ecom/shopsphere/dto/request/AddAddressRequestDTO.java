package com.ecom.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddAddressRequestDTO {

    @NotBlank(message = "Full name is required.")
    private String fullName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number."
    )
    private String phoneNumber;

    @NotBlank(message = "Address Line 1 is required.")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "State is required.")
    private String state;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "Postal code is required.")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "Postal code must be a valid 6-digit PIN code."
    )
    private String postalCode;

    private Boolean defaultAddress;
}