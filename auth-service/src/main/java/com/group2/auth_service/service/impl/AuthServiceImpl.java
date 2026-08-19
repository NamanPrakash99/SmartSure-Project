package com.group2.auth_service.service.impl;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import com.group2.auth_service.dto.AuthResponse;
import com.group2.auth_service.dto.LoginRequest;
import com.group2.auth_service.dto.RegisterRequest;
import com.group2.auth_service.dto.UpdateProfileRequest;
import com.group2.auth_service.entity.Role;
import com.group2.auth_service.entity.User;
import com.group2.auth_service.repository.AuthServiceRepository;
import com.group2.auth_service.security.JwtUtil;
import com.group2.auth_service.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final AuthServiceRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public AuthServiceImpl(AuthServiceRepository userRepository, 
	                  PasswordEncoder passwordEncoder, 
	                  JwtUtil jwtUtil) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}
	
	@PostConstruct
	public void initAdmin() {
		Optional<User> adminOpt = userRepository.findByEmail("admin@capgemini.com");
		if (adminOpt.isEmpty()) {
			User admin = new User();
			admin.setName("Admin");
			admin.setEmail("admin@capgemini.com");
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.setRole(Role.ADMIN);
			admin.setPhone("0000000000"); 
			admin.setAddress("Admin Address");
			userRepository.save(admin);
		}
	}

    @Transactional
	public User register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            throw new com.group2.auth_service.exception.UserAlreadyExistsException("Email is already registered.");
        } 
        
        // Password Validation
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long.");
        }
        if (!request.getPassword().matches(".*\\d.*")) {
            throw new RuntimeException("Password must contain at least one number.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));    
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(Role.CUSTOMER); 
        
        return userRepository.save(user);
	}

	public AuthResponse login(LoginRequest request) {
	    String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
	    logger.info("Login attempt for email: '{}'", email);
	    
	    Optional<User> userOpt = userRepository.findByEmail(email);
	    
	    if (userOpt.isEmpty()) {
	        userOpt = userRepository.findByEmail(request.getEmail());
	    }
	    
	    if (userOpt.isEmpty()) {
	    	logger.warn("Login Failed: User not found for email: {}", email);
	    	throw new RuntimeException("Invalid credentials: User not found. Please register first.");
	    }
	    
	    User user = userOpt.get();        

		if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
			return new AuthResponse(token, user.getRole().name(), user.getId(), user.getName());
		} else {
			logger.warn("Login Failed: Password does not match for email: {}", email);
			throw new RuntimeException("Invalid credentials: Password is incorrect.");
		}
	}

	public User getUserById(Long id) {
	    return userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
	}



    public java.util.List<User> getAllCustomers() {
        return userRepository.findByRole(Role.CUSTOMER);
    }

}
