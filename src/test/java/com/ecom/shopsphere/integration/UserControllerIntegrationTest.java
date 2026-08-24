package com.ecom.shopsphere.integration;

import com.ecom.shopsphere.dto.request.LoginRequestDTO;
import com.ecom.shopsphere.dto.request.RegisterRequestDTO;
import com.ecom.shopsphere.dto.request.UpdateProfileRequestDTO;
import com.ecom.shopsphere.dto.request.ChangePasswordRequestDTO;
import com.ecom.shopsphere.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private UserRepository userRepository;

    private String testToken;
    private String anotherToken;

    @BeforeEach
    void setupUsers() throws Exception {
        userRepository.deleteAll();

        RegisterRequestDTO user1 = RegisterRequestDTO.builder()
                .fullName("Profile User")
                .email("profileuser@gmail.com")
                .password("Password@123")
                .phoneNumber("9876543210")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(user1))
        ).andExpect(status().isCreated());

        testToken = extractToken("profileuser@gmail.com", "Password@123");

        RegisterRequestDTO user2 = RegisterRequestDTO.builder()
                .fullName("Another User")
                .email("anotheruser@gmail.com")
                .password("Password@456")
                .phoneNumber("9988776655")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(user2))
        ).andExpect(status().isCreated());

        anotherToken = extractToken("anotheruser@gmail.com", "Password@456");
    }

    private String extractToken(String email, String password) throws Exception {
        LoginRequestDTO login = LoginRequestDTO.builder()
                .email(email).password(password).build();

        MvcResult res = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(login))
                )
                .andExpect(status().isOk()).andReturn();

        return jsonMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("token").stringValue();
    }

    @Test
    void getProfile_Success_ReturnsFullDetails() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/profile")
                                .header("Authorization", "Bearer " + testToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profile fetched successfully"))
                .andExpect(jsonPath("$.data.email").value("profileuser@gmail.com"))
                .andExpect(jsonPath("$.data.fullName").value("Profile User"))
                .andExpect(jsonPath("$.data.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.userId").exists());
    }

    @Test
    void getProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_InvalidToken_Forbidden() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/profile")
                                .header("Authorization", "Bearer invalid.jwt.token")
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_FullUpdate_Success() throws Exception {
        UpdateProfileRequestDTO req = UpdateProfileRequestDTO.builder()
                .fullName("Updated Full Name")
                .phoneNumber("9999988888")
                .build();

        mockMvc.perform(
                        put("/api/v1/users/update-profile")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Full Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("9999988888"))
                .andExpect(jsonPath("$.data.email").value("profileuser@gmail.com"));
    }

    @Test
    void updateProfile_NullName_BadRequest() throws Exception {
        UpdateProfileRequestDTO req = UpdateProfileRequestDTO.builder()
                .fullName(null)
                .phoneNumber("9999988888")
                .build();

        mockMvc.perform(
                        put("/api/v1/users/update-profile")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfile_AnotherUser_Independent() throws Exception {

        UpdateProfileRequestDTO p1 = UpdateProfileRequestDTO.builder()
                .fullName("Name A")
                .phoneNumber("9111111111")
                .build();

        UpdateProfileRequestDTO p2 = UpdateProfileRequestDTO.builder()
                .fullName("Name B")
                .phoneNumber("9222222222")
                .build();

        // User 1 update
        mockMvc.perform(
                        put("/api/v1/users/update-profile")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(p1))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Name A"));

        // User 2 update
        mockMvc.perform(
                        put("/api/v1/users/update-profile")
                                .header("Authorization", "Bearer " + anotherToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(p2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Name B"));
    }

    @Test
    void changePassword_Success_ConfirmationReturned() throws Exception {
        ChangePasswordRequestDTO req = ChangePasswordRequestDTO.builder()
                .currentPassword("Password@123")
                .newPassword("NewPassword@456")
                .build();

        mockMvc.perform(
                        put("/api/v1/users/change-password")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confirmation").exists());
    }

    @Test
    void changePassword_WrongCurrentPassword_BadRequest() throws Exception {
        ChangePasswordRequestDTO req = ChangePasswordRequestDTO.builder()
                .currentPassword("WrongPassword@1")
                .newPassword("NewPassword@456")
                .build();

        mockMvc.perform(
                        put("/api/v1/users/change-password")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password Change Failed"));
    }

    @Test
    void changePassword_SameAsCurrentPassword_BadRequest() throws Exception {
        ChangePasswordRequestDTO req = ChangePasswordRequestDTO.builder()
                .currentPassword("Password@123")
                .newPassword("Password@123")
                .build();

        mockMvc.perform(
                        put("/api/v1/users/change-password")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_ShortNewPassword_BadRequest() throws Exception {
        ChangePasswordRequestDTO req = ChangePasswordRequestDTO.builder()
                .currentPassword("Password@123")
                .newPassword("s")
                .build();

        mockMvc.perform(
                        put("/api/v1/users/change-password")
                                .header("Authorization", "Bearer " + testToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAccount_Success_StateDeleted() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/users/delete-account")
                                .header("Authorization", "Bearer " + anotherToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("DELETED"));

        assertFalse(userRepository.existsByEmail("anotheruser@gmail.com"));
        assertTrue(userRepository.existsByEmail("profileuser@gmail.com"));
    }

    @Test
    void deleteAccount_DeletedUserCannotLogin() throws Exception {
        mockMvc.perform(
                delete("/api/v1/users/delete-account")
                        .header("Authorization", "Bearer " + testToken)
        ).andExpect(status().isOk());

        LoginRequestDTO login = LoginRequestDTO.builder()
                .email("profileuser@gmail.com").password("Password@123").build();

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(login))
                )
                .andExpect(status().isNotFound());
    }
}
