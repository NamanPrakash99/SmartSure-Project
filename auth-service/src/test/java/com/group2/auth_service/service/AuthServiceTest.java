package com.group2.auth_service.service;

import com.group2.auth_service.dto.AuthResponse;
import com.group2.auth_service.dto.LoginRequest;
import com.group2.auth_service.dto.RegisterRequest;
import com.group2.auth_service.dto.UpdateProfileRequest;
import com.group2.auth_service.entity.Role;
import com.group2.auth_service.entity.User;
import com.group2.auth_service.repository.AuthServiceRepository;
import com.group2.auth_service.security.JwtUtil;
import com.group2.auth_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private AuthServiceRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should create a new user successfully when registration details are valid")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("Password123");
        request.setName("Test User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        User result = authService.register(request);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return valid tokens when login credentials are correct")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("Password123");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashed_pass");
        user.setRole(Role.CUSTOMER);
        user.setId(1L);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);
        assertEquals("jwt_token", response.getToken());
    }

    @Test
    @DisplayName("Should throw exception when attempting to login with non-existent user")
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        LoginRequest request = new LoginRequest();
        request.setEmail("none@none.com");
        request.setPassword("pass");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }



    @Test
    @DisplayName("Should update user profile details correctly")
    void shouldUpdateUserProfileSuccessfully() {
        Long userId = 1L;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        request.setPhone("9876543210");
        request.setAddress("New Address");

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.updateUser(userId, request);

        assertEquals("New Name", result.getName());
        assertEquals("9876543210", result.getPhone());
        assertEquals("New Address", result.getAddress());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should return list of all customers")
    void shouldReturnAllCustomers() {
        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(List.of(new User(), new User()));
        
        List<User> customers = authService.getAllCustomers();
        
        assertEquals(2, customers.size());
        verify(userRepository).findByRole(Role.CUSTOMER);
    }

    @Test
    @DisplayName("Should throw exception when registering existing user")
    void shouldThrowExceptionWhenRegisteringExistingUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThrows(com.group2.auth_service.exception.UserAlreadyExistsException.class, () -> authService.register(request));
    }







    @Test
    @DisplayName("Should throw exception when password incorrect during login")
    void shouldThrowExceptionWhenPasswordIncorrectDuringLogin() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("WrongPassword");

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashed_pass");
        user.setRole(Role.CUSTOMER);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = authService.getUserById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when get user by id not found")
    void shouldThrowExceptionWhenGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.getUserById(1L));
    }




}
