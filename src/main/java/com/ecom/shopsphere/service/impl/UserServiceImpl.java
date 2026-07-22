package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.EmailAlreadyExistsException;
import com.ecom.shopsphere.exception.InvalidCredentialsException;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {

        log.info("Starting user registration for email: {}", request.getEmail());

        log.info("Checking if email already exists: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        log.info("Mapping RegisterRequestDTO to User entity");

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.USER)
                .build();

        log.debug("User entity created successfully");

        User savedUser = userRepository.save(user);

        log.info(
                "User registered successfully. User ID: {}, Email: {}",
                savedUser.getId(),
                savedUser.getEmail()
        );

        log.info("Registration process completed successfully for email: {}", savedUser.getEmail());

        return RegisterResponseDTO.builder()
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .build();
    }
    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO request) {

        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            log.warn("Invalid password for email: {}", request.getEmail());

            throw new InvalidCredentialsException("Invalid email or password.");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        log.info("JWT token generated successfully for email: {}", user.getEmail());

        return LoginResponseDTO.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .token(token)
                .build();
    }
}
