package com.ecom.shopsphere.mapper;

import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.ProfileResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.entity.User;

public interface UserMapper {

    User toEntity(RegisterRequestDTO request);

    RegisterResponseDTO toRegisterResponse(User user);

    LoginResponseDTO toLoginResponse(User user);

    ProfileResponseDTO toProfileResponse(User user);

    void updateUserFromRequest(UpdateProfileRequestDTO request, User user);
}
