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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerUser_Success() {

        RegisterRequestDTO request = RegisterRequestDTO.builder().email("test@gmail.com").password("password123").fullName("Test User").phoneNumber("9876543210").build();


        User user = User.builder().userId(1L).email("test@gmail.com").password("encodedPassword").build();


        User savedUser = User.builder().userId(1L).email("test@gmail.com").password("encodedPassword").build();


        RegisterResponseDTO response = RegisterResponseDTO.builder().userId(1L).email("test@gmail.com").build();


        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);


        when(userMapper.toEntity(request)).thenReturn(user);


        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");


        when(userRepository.save(user)).thenReturn(savedUser);


        when(userMapper.toRegisterResponse(savedUser)).thenReturn(response);


        RegisterResponseDTO result = authService.registerUser(request);


        assertNotNull(result);

        assertEquals("test@gmail.com", result.getEmail());


        assertEquals(1L, result.getUserId());

        // Verify password was encoded

        verify(passwordEncoder).encode("password123");


        // Verify role

        assertEquals(Role.USER, user.getRole());


        // Verify cart created

        verify(cartRepository).save(any(Cart.class));


        // Verify wishlist created

        verify(wishlistRepository).save(any(Wishlist.class));

        verify(userRepository).existsByEmail("test@gmail.com");

        // Verify user saved

        verify(userRepository).save(user);


        // Verify response mapping

        verify(userMapper).toRegisterResponse(savedUser);

    }

    @Test
    void registerUser_EmailAlreadyExists() {


        RegisterRequestDTO request = RegisterRequestDTO.builder().email("existing@gmail.com").password("password123").fullName("Existing User").phoneNumber("9876543210").build();


        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);


        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> authService.registerUser(request));


        assertEquals("Email is already registered.", exception.getMessage());


        verify(userRepository).existsByEmail("existing@gmail.com");


        verify(userRepository, never()).save(any(User.class));


        verify(passwordEncoder, never()).encode(anyString());

        verify(cartRepository, never()).save(any(Cart.class));

        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void loginUser_Success() {


        LoginRequestDTO request = LoginRequestDTO.builder().email("test@gmail.com").password("password123").build();


        User user = User.builder().userId(1L).email("test@gmail.com").password("encodedPassword").build();


        LoginResponseDTO response = LoginResponseDTO.builder().userId(1L).email("test@gmail.com").token("jwt-token").build();


        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.of(user));


        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);


        when(jwtService.generateToken(user.getEmail())).thenReturn("jwt-token");


        when(userMapper.toLoginResponse(user)).thenReturn(response);


        LoginResponseDTO result = authService.loginUser(request);


        assertNotNull(result);


        assertEquals("jwt-token", result.getToken());


        assertEquals("test@gmail.com", result.getEmail());


        verify(userRepository).findByEmail("test@gmail.com");


        verify(passwordEncoder).matches("password123", "encodedPassword");


        verify(jwtService).generateToken("test@gmail.com");


        verify(userMapper).toLoginResponse(user);
    }

    @Test
    void loginUser_Invalid() {


        LoginRequestDTO request = LoginRequestDTO.builder().email("wrong@gmail.com").password("password123").build();


        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.empty());


        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.loginUser(request));


        assertEquals("User not found.", exception.getMessage());


        verify(userRepository).findByEmail("wrong@gmail.com");


        verify(passwordEncoder, never()).matches(anyString(), anyString());


        verify(jwtService, never()).generateToken(anyString());


        verify(userMapper, never()).toLoginResponse(any(User.class));
    }

    @Test
    void loginUser_InvalidPassword() {

        LoginRequestDTO request = LoginRequestDTO.builder().email("test@gmail.com").password("wrongPassword").build();


        User user = User.builder().userId(1L).email("test@gmail.com").password("encodedPassword").build();


        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.of(user));


        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);


        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(request));


        assertEquals("Invalid password for email.", exception.getMessage());


        verify(userRepository).findByEmail("test@gmail.com");


        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");


        verify(jwtService, never()).generateToken(anyString());


        verify(userMapper, never()).toLoginResponse(any(User.class));
    }
}