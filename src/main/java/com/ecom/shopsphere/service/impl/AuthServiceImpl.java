package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.entity.Cart;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.entity.Wishlist;
import com.ecom.shopsphere.exception.EmailAlreadyExistsException;
import com.ecom.shopsphere.exception.InvalidCredentialsException;
import com.ecom.shopsphere.exception.UserNotFoundException;
import com.ecom.shopsphere.mapper.UserMapper;
import com.ecom.shopsphere.repository.CartRepository;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.repository.WishlistRepository;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserMapper userMapper;

    private final CartRepository cartRepository;

    private final WishlistRepository wishlistRepository;

    @Override
    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {

        log.info("Starting user registration for email: {}", request.getEmail());

        log.info("Checking if email already exists: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        log.info("Mapping RegisterRequestDTO to User entity");

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        log.debug("User entity created successfully");

        User savedUser = userRepository.save(user);

        log.info(
                "User registered successfully. User ID: {}, Email: {}",
                savedUser.getUserId(),
                savedUser.getEmail()
        );

        Cart cart = Cart.builder()
                .user(savedUser)
                .build();

        cartRepository.save(cart);

        log.info(
                "Cart created successfully for user ID: {}",
                savedUser.getUserId()
        );

        Wishlist wishlist = Wishlist.builder()
                .user(savedUser)
                .build();

        wishlistRepository.save(wishlist);

        log.info(
                "Wishlist created successfully for user ID: {}",
                savedUser.getUserId()
        );

        log.info("Registration process completed successfully for email: {}", savedUser.getEmail());

        RegisterResponseDTO response = userMapper.toRegisterResponse(savedUser);

        return response;
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO request) {

        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    return new UserNotFoundException("User not found.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            log.warn("Invalid password for email: {}", request.getEmail());

            throw new InvalidCredentialsException("Invalid password for email.");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        log.info("JWT token generated successfully for email: {}", user.getEmail());

        LoginResponseDTO response = userMapper.toLoginResponse(user);

        response.setToken(token);

        return response;
    }
}
