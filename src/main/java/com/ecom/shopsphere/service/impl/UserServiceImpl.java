package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.*;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.EmailAlreadyExistsException;
import com.ecom.shopsphere.exception.InvalidCredentialsException;
import com.ecom.shopsphere.exception.PasswordChangeFailedException;
import com.ecom.shopsphere.exception.UserNotFoundException;
import com.ecom.shopsphere.mapper.UserMapper;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserMapper userMapper;

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
                savedUser.getId(),
                savedUser.getEmail()
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
                    return new InvalidCredentialsException("Invalid email or password.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            log.warn("Invalid password for email: {}", request.getEmail());

            throw new InvalidCredentialsException("Invalid email or password.");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());

        log.info("JWT token generated successfully for email: {}", user.getEmail());

        LoginResponseDTO response = userMapper.toLoginResponse(user);

        response.setToken(token);

        return response;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        log.info("Logged-in user email: {}", email);

//        User user = userRepository.findByEmail("dummy@gmail.com")
//                .orElseThrow(() ->
//                        new UserNotFoundException("User not found."));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {

                    log.error("User not found: {}", email);

                    return new UserNotFoundException("User not found.");
                });
    }

    @Override
    public ProfileResponseDTO getProfile() {

        log.info("Fetching logged-in user profile.");

        User user = getCurrentUser();

        log.info("Profile fetched successfully: {}", user.getEmail());

        return userMapper.toProfileResponse(user);
    }

    @Override
    public ProfileResponseDTO updateProfile(UpdateProfileRequestDTO request) {

        log.info("Updating logged-in user profile.");

        User user = getCurrentUser();

        userMapper.updateUserFromRequest(request, user);

        User updatedUser = userRepository.save(user);

        log.info("Profile updated successfully: {}", updatedUser.getEmail());

        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    public ChangePasswordResponseDTO changePassword(
            ChangePasswordRequestDTO request) {

        log.info("Changing password for logged-in user.");

        User user = getCurrentUser();

        log.info("Verifying current password for user: {}", user.getEmail());

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            log.warn("Current password is incorrect for user: {}", user.getEmail());

            throw new PasswordChangeFailedException(
                    "Current password is incorrect.");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new PasswordChangeFailedException(
                    "New password must be different from the current password.");
        }

        log.info("Encoding new password.");

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());

        return ChangePasswordResponseDTO.builder()
                .confirmation("Password updated successfully.")
                .build();
    }

    @Override
    public DeleteAccountResponseDTO deleteAccount() {

        log.info("Deleting logged-in user account.");

        User user = getCurrentUser();

        log.info("Deleting account for user: {}", user.getEmail());

        userRepository.delete(user);

        log.info("Account deleted successfully for user: {}", user.getEmail());

        return DeleteAccountResponseDTO.builder()
                .details("Your account has been deleted successfully.")
                .build();
    }
}
