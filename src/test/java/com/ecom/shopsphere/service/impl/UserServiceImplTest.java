package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.ChangePasswordResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteAccountResponseDTO;
import com.ecom.shopsphere.dto.response.ProfileResponseDTO;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.PasswordChangeFailedException;
import com.ecom.shopsphere.mapper.UserMapper;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    // IMPORTANT:
    // Your current UserServiceImpl uses this service
    @Mock
    private CurrentUserService currentUserService;


    // =========================================================
    // GET PROFILE - SUCCESS
    // =========================================================

    @Test
    void getProfile_Success() {

        Long userId = 1L;

        User user = User.builder().userId(userId).fullName("Test User").email("test@gmail.com").phoneNumber("9876543210").build();

        ProfileResponseDTO response = ProfileResponseDTO.builder().userId(userId).fullName("Test User").email("test@gmail.com").phoneNumber("9876543210").build();

        // CurrentUserService gives the logged-in user
        when(currentUserService.getCurrentUser()).thenReturn(user);

        // Mapper converts User -> ProfileResponseDTO
        when(userMapper.toProfileResponse(user)).thenReturn(response);

        ProfileResponseDTO result = userService.getProfile();

        assertNotNull(result);

        assertEquals("test@gmail.com", result.getEmail());

        assertEquals("Test User", result.getFullName());

        assertEquals("9876543210", result.getPhoneNumber());

        verify(currentUserService).getCurrentUser();

        verify(userMapper).toProfileResponse(user);
    }


    // =========================================================
    // UPDATE PROFILE - SUCCESS
    // =========================================================

    @Test
    void updateProfile_Success() {

        Long userId = 1L;

        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder().fullName("Updated User").phoneNumber("9999999999").build();

        User existingUser = User.builder().userId(userId).fullName("Old User").email("test@gmail.com").phoneNumber("8888888888").build();

        User updatedUser = User.builder().userId(userId).fullName("Updated User").email("test@gmail.com").phoneNumber("9999999999").build();

        ProfileResponseDTO response = ProfileResponseDTO.builder().userId(userId).fullName("Updated User").email("test@gmail.com").phoneNumber("9999999999").build();

        // Logged-in user
        when(currentUserService.getCurrentUser()).thenReturn(existingUser);

        // Mapper updates existing User object
        doNothing().when(userMapper).updateUserFromRequest(request, existingUser);

        // Repository saves the updated user
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        // Mapper converts updated user to response
        when(userMapper.toProfileResponse(updatedUser)).thenReturn(response);

        ProfileResponseDTO result = userService.updateProfile(request);

        assertNotNull(result);

        assertEquals("Updated User", result.getFullName());

        assertEquals("9999999999", result.getPhoneNumber());

        verify(currentUserService).getCurrentUser();

        verify(userMapper).updateUserFromRequest(request, existingUser);

        verify(userRepository).save(existingUser);

        verify(userMapper).toProfileResponse(updatedUser);
    }


    // =========================================================
    // CHANGE PASSWORD - SUCCESS
    // =========================================================

    @Test
    void changePassword_Success() {

        Long userId = 1L;

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder().currentPassword("oldPassword").newPassword("newPassword123").build();

        User user = User.builder().userId(userId).email("test@gmail.com").password("encodedOldPassword").build();

        // VERY IMPORTANT:
        // Your service calls currentUserService.getCurrentUser()
        when(currentUserService.getCurrentUser()).thenReturn(user);

        // Current password is correct
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        // New password is NOT the same as old password
        when(passwordEncoder.matches("newPassword123", "encodedOldPassword")).thenReturn(false);

        // New password gets encoded
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // Save user
        when(userRepository.save(user)).thenReturn(user);

        ChangePasswordResponseDTO result = userService.changePassword(request);

        assertNotNull(result);

        assertEquals("Password updated successfully.", result.getConfirmation());

        assertEquals("encodedNewPassword", user.getPassword());

        verify(currentUserService).getCurrentUser();

        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");

        verify(passwordEncoder).matches("newPassword123", "encodedOldPassword");

        verify(passwordEncoder).encode("newPassword123");

        verify(userRepository).save(user);
    }


    // =========================================================
    // CHANGE PASSWORD - CURRENT PASSWORD INCORRECT
    // =========================================================

    @Test
    void changePassword_CurrentPasswordIncorrect() {

        Long userId = 1L;

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder().currentPassword("wrongPassword").newPassword("newPassword123").build();

        User user = User.builder().userId(userId).email("test@gmail.com").password("encodedOldPassword").build();

        // IMPORTANT:
        // Without this, currentUserService.getCurrentUser()
        // returns null -> user.getEmail() causes NPE.
        when(currentUserService.getCurrentUser()).thenReturn(user);

        // Current password is WRONG
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        PasswordChangeFailedException exception = assertThrows(PasswordChangeFailedException.class, () -> userService.changePassword(request));

        assertEquals("Current password is incorrect.", exception.getMessage());

        verify(currentUserService).getCurrentUser();

        verify(passwordEncoder).matches("wrongPassword", "encodedOldPassword");

        // Because password is incorrect,
        // new password must never be encoded.
        verify(passwordEncoder, never()).encode(anyString());

        // User must never be saved.
        verify(userRepository, never()).save(any(User.class));
    }


    // =========================================================
    // CHANGE PASSWORD - NEW PASSWORD SAME AS OLD
    // =========================================================

    @Test
    void changePassword_NewPasswordSameAsOld() {

        Long userId = 1L;

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder().currentPassword("oldPassword").newPassword("oldPassword").build();

        User user = User.builder().userId(userId).email("test@gmail.com").password("encodedOldPassword").build();

        // Logged-in user
        when(currentUserService.getCurrentUser()).thenReturn(user);

        // Current password is correct
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        /*
         * The service checks the new password again.
         *
         * Since the new password is also "oldPassword",
         * this returns true.
         */
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        PasswordChangeFailedException exception = assertThrows(PasswordChangeFailedException.class, () -> userService.changePassword(request));

        assertEquals("New password must be different from the current password.", exception.getMessage());

        verify(currentUserService).getCurrentUser();

        verify(passwordEncoder, atLeastOnce()).matches("oldPassword", "encodedOldPassword");

        // Password should NOT be encoded
        verify(passwordEncoder, never()).encode(anyString());

        // User should NOT be saved
        verify(userRepository, never()).save(any(User.class));
    }


    // =========================================================
    // DELETE ACCOUNT - SUCCESS
    // =========================================================

    @Test
    void deleteAccount_Success() {

        Long userId = 1L;

        User user = User.builder().userId(userId).email("test@gmail.com").fullName("Test User").build();

        // Current logged-in user
        when(currentUserService.getCurrentUser()).thenReturn(user);

        DeleteAccountResponseDTO result = userService.deleteAccount();

        assertNotNull(result);

        assertEquals("DELETED", result.getState());

        verify(currentUserService).getCurrentUser();

        verify(userRepository).delete(user);
    }
}