package com.ecom.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDTO {

    @NotBlank(message = "Full name is required.")
    private String fullName;

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must contain exactly 10 digits."
    )
    private String phoneNumber;
}