package com.ecom.shopsphere.controller;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.response.LoginResponseDTO;
import com.ecom.shopsphere.dto.response.RegisterResponseDTO;
import com.ecom.shopsphere.exception.EmailAlreadyExistsException;
import com.ecom.shopsphere.exception.InvalidCredentialsException;
import com.ecom.shopsphere.security.JwtService;
import com.ecom.shopsphere.service.AuthService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void register_Success() throws Exception {

        RegisterRequestDTO request =
                RegisterRequestDTO.builder()
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();

        RegisterResponseDTO response =
                RegisterResponseDTO.builder()
                        .userId(1L)
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .phoneNumber("9876543210")
                        .build();

        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenReturn(response);


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Registration successful"))
                .andExpect(jsonPath("$.data.userId")
                        .value(1))
                .andExpect(jsonPath("$.data.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.data.email")
                        .value("john@gmail.com"))
                .andExpect(jsonPath("$.data.phoneNumber")
                        .value("9876543210"));

        verify(authService)
                .registerUser(any(RegisterRequestDTO.class));
    }


    @Test
    void login_Success() throws Exception {

        LoginRequestDTO request =
                LoginRequestDTO.builder()
                        .email("john@gmail.com")
                        .password("Password@123")
                        .build();

        LoginResponseDTO response =
                LoginResponseDTO.builder()
                        .userId(1L)
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .token("jwt-token")
                        .build();

        when(authService.loginUser(any(LoginRequestDTO.class)))
                .thenReturn(response);


        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Login successful"))
                .andExpect(jsonPath("$.data.email")
                        .value("john@gmail.com"))
                .andExpect(jsonPath("$.data.token")
                        .value("jwt-token"));

        verify(authService)
                .loginUser(any(LoginRequestDTO.class));
    }


    @Test
    void register_EmailAlreadyExists() throws Exception {

        RegisterRequestDTO request =
                RegisterRequestDTO.builder()
                        .fullName("John Doe")
                        .email("john@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();

        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(
                        new EmailAlreadyExistsException(
                                "Registration Failed"
                        )
                );


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Registration Failed"));

        verify(authService)
                .registerUser(any(RegisterRequestDTO.class));
    }


    @Test
    void login_InvalidCredentials() throws Exception {

        LoginRequestDTO request =
                LoginRequestDTO.builder()
                        .email("john@gmail.com")
                        .password("WrongPassword")
                        .build();

        when(authService.loginUser(any(LoginRequestDTO.class)))
                .thenThrow(
                        new InvalidCredentialsException(
                                "Login Failed"
                        )
                );


        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Login Failed"));

        verify(authService)
                .loginUser(any(LoginRequestDTO.class));
    }


    @Test
    void register_InvalidRequest() throws Exception {

        RegisterRequestDTO request =
                RegisterRequestDTO.builder()
                        .fullName("")
                        .email("invalid-email")
                        .password("")
                        .phoneNumber("123")
                        .build();


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerUser(any(RegisterRequestDTO.class));
    }
}