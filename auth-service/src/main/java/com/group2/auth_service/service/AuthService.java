package com.group2.auth_service.service;

import java.util.List;
import com.group2.auth_service.dto.AuthResponse;
import com.group2.auth_service.dto.LoginRequest;
import com.group2.auth_service.dto.RegisterRequest;
import com.group2.auth_service.dto.UpdateProfileRequest;
import com.group2.auth_service.entity.User;

public interface AuthService {
    void initAdmin();
    User register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getUserById(Long id);
    List<User> getAllCustomers();
}
