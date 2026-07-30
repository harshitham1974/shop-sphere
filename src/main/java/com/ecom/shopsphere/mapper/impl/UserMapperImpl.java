package com.ecom.shopsphere.mapper.impl;

import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.ProfileResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(RegisterRequestDTO request) {
        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    @Override
    public RegisterResponseDTO toRegisterResponse(User user) {
        return RegisterResponseDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public LoginResponseDTO toLoginResponse(User user) {
        return LoginResponseDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public ProfileResponseDTO toProfileResponse(User user) {

        return ProfileResponseDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

    @Override
    public void updateUserFromRequest(
            UpdateProfileRequestDTO request,
            User user) {

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
    }
}
