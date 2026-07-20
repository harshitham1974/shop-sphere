package com.ecom.shopsphere.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    private Long userId;

    private String fullName;

    private String email;

    private String message;
}