package com.ecom.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RegisterResponseDTO {

    private Long userId;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String message;

}