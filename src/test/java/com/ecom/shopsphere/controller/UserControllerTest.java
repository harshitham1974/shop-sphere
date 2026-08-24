package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.response.ChangePasswordResponseDTO;
import com.ecom.shopsphere.dto.response.DeleteAccountResponseDTO;
import com.ecom.shopsphere.dto.response.ProfileResponseDTO;
import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.exception.InvalidCredentialsException;
import com.ecom.shopsphere.exception.UserNotFoundException;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void getProfile_Success() throws Exception {

        ProfileResponseDTO response =
                ProfileResponseDTO.builder()
                        .userId(1L)
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .phoneNumber("9876543210")
                        .role(Role.USER)
                        .build();

        when(userService.getProfile())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/users/profile")
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Profile fetched successfully"))
                .andExpect(jsonPath("$.data.userId")
                        .value(1))
                .andExpect(jsonPath("$.data.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.data.email")
                        .value("john@gmail.com"))
                .andExpect(jsonPath("$.data.phoneNumber")
                        .value("9876543210"))
                .andExpect(jsonPath("$.data.role")
                        .value("USER"));

        verify(userService)
                .getProfile();
    }

    @Test
    void updateProfile_Success() throws Exception {

        UpdateProfileRequestDTO request =
                UpdateProfileRequestDTO.builder()
                        .fullName("John Doe Updated")
                        .phoneNumber("9998887770")
                        .build();

        ProfileResponseDTO response =
                ProfileResponseDTO.builder()
                        .userId(1L)
                        .fullName("John Doe Updated")
                        .email("john@gmail.com")
                        .phoneNumber("9998887770")
                        .role(Role.USER)
                        .build();

        when(userService.updateProfile(any(UpdateProfileRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/users/update-profile")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.userId")
                        .value(1))
                .andExpect(jsonPath("$.data.fullName")
                        .value("John Doe Updated"))
                .andExpect(jsonPath("$.data.phoneNumber")
                        .value("9998887770"));

        verify(userService)
                .updateProfile(any(UpdateProfileRequestDTO.class));
    }

    @Test
    void changePassword_Success() throws Exception {

        ChangePasswordRequestDTO request =
                ChangePasswordRequestDTO.builder()
                        .currentPassword("OldPassword@123")
                        .newPassword("NewPassword@456")
                        .build();

        ChangePasswordResponseDTO response =
                ChangePasswordResponseDTO.builder()
                        .confirmation("Password changed successfully")
                        .build();

        when(userService.changePassword(any(ChangePasswordRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/users/change-password")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Password changed successfully"))
                .andExpect(jsonPath("$.data.confirmation")
                        .value("Password changed successfully"));

        verify(userService)
                .changePassword(any(ChangePasswordRequestDTO.class));
    }

    @Test
    void deleteAccount_Success() throws Exception {

        DeleteAccountResponseDTO response =
                DeleteAccountResponseDTO.builder()
                        .state("DELETED")
                        .build();

        when(userService.deleteAccount())
                .thenReturn(response);

        mockMvc.perform(
                        delete("/api/v1/users/delete-account")
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Account deleted successfully"))
                .andExpect(jsonPath("$.data.state")
                        .value("DELETED"));

        verify(userService)
                .deleteAccount();
    }

    @Test
    void changePassword_InvalidCredentialsException() throws Exception {

        ChangePasswordRequestDTO request =
                ChangePasswordRequestDTO.builder()
                        .currentPassword("WrongPassword")
                        .newPassword("NewPassword@456")
                        .build();

        when(userService.changePassword(any(ChangePasswordRequestDTO.class)))
                .thenThrow(
                        new InvalidCredentialsException(
                                "Login Failed"
                        )
                );

        mockMvc.perform(
                        put("/api/v1/users/change-password")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Login Failed"));

        verify(userService)
                .changePassword(any(ChangePasswordRequestDTO.class));
    }

    @Test
    void getProfile_UserNotFoundException() throws Exception {

        when(userService.getProfile())
                .thenThrow(
                        new UserNotFoundException(
                                "User Not Found"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/users/profile")
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User Not Found"));

        verify(userService)
                .getProfile();
    }
}
