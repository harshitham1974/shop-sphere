package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.*;
import com.ecom.shopsphere.entity.Cart;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.entity.Wishlist;
import com.ecom.shopsphere.exception.EmailAlreadyExistsException;
import com.ecom.shopsphere.exception.InvalidCredentialsException;
import com.ecom.shopsphere.exception.PasswordChangeFailedException;
import com.ecom.shopsphere.mapper.UserMapper;
import com.ecom.shopsphere.repository.CartRepository;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.repository.WishlistRepository;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.CurrentUserService;
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

    private final UserMapper userMapper;

    private final CurrentUserService currentUserService;

    @Override
    public ProfileResponseDTO getProfile() {

        log.info("Fetching logged-in user profile.");

        User user = currentUserService.getCurrentUser();

        log.info("Profile fetched successfully: {}", user.getEmail());

        return userMapper.toProfileResponse(user);
    }

    @Override
    public ProfileResponseDTO updateProfile(UpdateProfileRequestDTO request) {

        log.info("Updating logged-in user profile.");

        User user = currentUserService.getCurrentUser();

        userMapper.updateUserFromRequest(request, user);

        User updatedUser = userRepository.save(user);

        log.info("Profile updated successfully: {}", updatedUser.getEmail());

        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    public ChangePasswordResponseDTO changePassword(
            ChangePasswordRequestDTO request) {

        log.info("Changing password for logged-in user.");

        User user = currentUserService.getCurrentUser();

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

        User user = currentUserService.getCurrentUser();

        log.info("Deleting account for user: {}", user.getEmail());

        userRepository.delete(user);

        log.info("Account deleted successfully for user: {}", user.getEmail());

        return DeleteAccountResponseDTO.builder()
                .state("DELETED")
                .build();
    }
}
