package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.*;

public interface UserService {

    RegisterResponseDTO registerUser(RegisterRequestDTO request);

    LoginResponseDTO loginUser(LoginRequestDTO request);

    ProfileResponseDTO getProfile();

    ProfileResponseDTO updateProfile(UpdateProfileRequestDTO request);

    ChangePasswordResponseDTO changePassword(ChangePasswordRequestDTO request);

    DeleteAccountResponseDTO deleteAccount();

}