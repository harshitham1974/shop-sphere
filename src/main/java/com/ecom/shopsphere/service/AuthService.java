package com.ecom.shopsphere.service;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;

public interface AuthService {


    RegisterResponseDTO registerUser(RegisterRequestDTO request);

    LoginResponseDTO loginUser(LoginRequestDTO request);
}
