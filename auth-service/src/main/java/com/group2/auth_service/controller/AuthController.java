package com.group2.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group2.auth_service.dto.AuthResponse;
import com.group2.auth_service.dto.LoginRequest;
import com.group2.auth_service.dto.RegisterRequest;
import com.group2.auth_service.entity.User;
import com.group2.auth_service.service.AuthService;
import com.group2.auth_service.security.JwtUtil;
import com.group2.auth_service.security.JwtUtil;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final AuthService service;
	private final JwtUtil jwtUtil;

	public AuthController(AuthService service, JwtUtil jwtUtil) {
		this.service = service;
		this.jwtUtil = jwtUtil;
	}
	

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }


    
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
	    	return ResponseEntity.ok(service.register(request));
    }
    


    @org.springframework.web.bind.annotation.GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }



    @org.springframework.web.bind.annotation.GetMapping("/customers")
    public java.util.List<User> getAllCustomers() {
        return service.getAllCustomers();
    }

}
