package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.repository.CartRepository;
import com.ecom.shopsphere.repository.UserRepository;
import com.ecom.shopsphere.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registerUser_Success() throws Exception {

        RegisterRequestDTO request =
                RegisterRequestDTO.builder()
                        .fullName("Integration User")
                        .email("integration@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email")
                        .value("integration@gmail.com"));


        // Verify real database

        assertTrue(
                userRepository.existsByEmail(
                        "integration@gmail.com"
                )
        );
    }
    @Test
    void registerUser_EmailAlreadyExists() throws Exception {

        // First registration

        RegisterRequestDTO firstRequest =
                RegisterRequestDTO.builder()
                        .fullName("First User")
                        .email("duplicate@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();


        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(firstRequest)
                        )
        );


        // Second registration with same email

        RegisterRequestDTO secondRequest =
                RegisterRequestDTO.builder()
                        .fullName("Second User")
                        .email("duplicate@gmail.com")
                        .password("Password@456")
                        .phoneNumber("9876543211")
                        .build();


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(secondRequest)
                                )
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Registration Failed"));
    }
    @Test
    void loginUser_Success() throws Exception {

        // Register user first

        RegisterRequestDTO registerRequest =
                RegisterRequestDTO.builder()
                        .fullName("Login User")
                        .email("login@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(
                                        registerRequest
                                )
                        )
        );


        // Login

        LoginRequestDTO loginRequest =
                LoginRequestDTO.builder()
                        .email("login@gmail.com")
                        .password("Password@123")
                        .build();


        String response =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                jsonMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.email")
                                .value("login@gmail.com"))
                        .andExpect(jsonPath("$.data.token")
                                .exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        assertNotNull(response);
    }
    @Test
    void loginUser_InvalidPassword() throws Exception {

        // Register real user first

        RegisterRequestDTO registerRequest =
                RegisterRequestDTO.builder()
                        .fullName("Invalid Password User")
                        .email("wrongpassword@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                registerRequest
                                        )
                                )
                )
                .andExpect(status().isCreated());


        // Login using wrong password

        LoginRequestDTO loginRequest =
                LoginRequestDTO.builder()
                        .email("wrongpassword@gmail.com")
                        .password("WrongPassword@123")
                        .build();


        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Login Failed"));
    }
    @Test
    void loginUser_UserNotFound() throws Exception {

        LoginRequestDTO loginRequest =
                LoginRequestDTO.builder()
                        .email("doesnotexist@gmail.com")
                        .password("Password@123")
                        .build();

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User Not Found"));
    }
    @Test
    void getCart_WithValidJwt_Success() throws Exception {

        // 1. Register user

        RegisterRequestDTO registerRequest =
                RegisterRequestDTO.builder()
                        .fullName("JWT User")
                        .email("jwtuser@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(
                                                registerRequest
                                        )
                                )
                )
                .andExpect(status().isCreated());


        // 2. Login

        LoginRequestDTO loginRequest =
                LoginRequestDTO.builder()
                        .email("jwtuser@gmail.com")
                        .password("Password@123")
                        .build();

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                jsonMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        // 3. Extract JWT

        String loginResponse =
                loginResult
                        .getResponse()
                        .getContentAsString();

        String token =
                jsonMapper
                        .readTree(loginResponse)
                        .get("data")
                        .get("token").stringValue();;


        assertNotNull(token);
        assertFalse(token.isBlank());


        // 4. Access protected Cart endpoint

        mockMvc.perform(
                        get("/api/v1/cart")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    void getCart_WithoutJwt_Unauthorized() throws Exception {

        mockMvc.perform(
                        get("/api/v1/cart")
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    void getCart_WithInvalidJwt_Unauthorized() throws Exception {

        String invalidToken =
                "eyJhbGciOiJIUzI1NiJ9.invalid.token";

        mockMvc.perform(
                        get("/api/v1/cart")
                                .header(
                                        "Authorization",
                                        "Bearer " + invalidToken
                                )
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    void registerUser_DuplicateEmail() throws Exception {

        RegisterRequestDTO request =
                RegisterRequestDTO.builder()
                        .fullName("Duplicate User")
                        .email("duplicateemail@gmail.com")
                        .password("Password@123")
                        .phoneNumber("9876543210")
                        .build();

        // First registration
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        jsonMapper.writeValueAsString(request)
                                )
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Registration Failed"));
    }
}