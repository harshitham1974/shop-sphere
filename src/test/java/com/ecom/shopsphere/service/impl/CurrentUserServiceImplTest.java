package com.ecom.shopsphere.service.impl;

import com.ecom.shopsphere.entity.Role;
import com.ecom.shopsphere.entity.User;
import com.ecom.shopsphere.exception.UserNotFoundException;
import com.ecom.shopsphere.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CurrentUserServiceImpl currentUserService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMocked;

    @BeforeEach
    void setup() {
        securityContextHolderMocked = mockStatic(SecurityContextHolder.class);
        securityContextHolderMocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMocked.close();
    }

    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .email("currentuser@test.com")
                .fullName("Current User")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void getCurrentUser_Success() {
        when(authentication.getName()).thenReturn("currentuser@test.com");
        User user = createTestUser();
        when(userRepository.findByEmail("currentuser@test.com")).thenReturn(Optional.of(user));

        User result = currentUserService.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("currentuser@test.com", result.getEmail());
        assertEquals("Current User", result.getFullName());

        verify(authentication).getName();
        verify(userRepository).findByEmail("currentuser@test.com");
    }

    @Test
    void getCurrentUser_AdminUser_Success() {
        when(authentication.getName()).thenReturn("admin@test.com");
        User admin = User.builder()
                .userId(99L)
                .email("admin@test.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        User result = currentUserService.getCurrentUser();

        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals("admin@test.com", result.getEmail());
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsException() {
        when(authentication.getName()).thenReturn("nonexistent@test.com");
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> currentUserService.getCurrentUser()
        );

        assertEquals("User not found.", exception.getMessage());

        verify(authentication).getName();
        verify(userRepository).findByEmail("nonexistent@test.com");
    }

    @Test
    void getCurrentUser_AuthenticationCalled() {
        when(authentication.getName()).thenReturn("a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(createTestUser()));

        currentUserService.getCurrentUser();

        securityContextHolderMocked.verify(SecurityContextHolder::getContext);
        verify(securityContext).getAuthentication();
    }
}
