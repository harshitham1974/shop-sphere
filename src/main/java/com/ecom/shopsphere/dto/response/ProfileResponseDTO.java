package com.ecom.shopsphere.dto.response;

import com.ecom.shopsphere.entity.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseDTO {
    private Long userId;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;
}
