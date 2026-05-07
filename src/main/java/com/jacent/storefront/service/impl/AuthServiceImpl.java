package com.jacent.storefront.service.impl;

import com.jacent.storefront.configuration.JwtConfig;
import com.jacent.storefront.dto.request.LoginRequest;
import com.jacent.storefront.dto.request.RefreshTokenRequest;
import com.jacent.storefront.dto.request.RegisterRequest;
import com.jacent.storefront.dto.response.AuthResponse;
import com.jacent.storefront.dto.response.UserResponse;
import com.jacent.storefront.entity.User;
import com.jacent.storefront.repository.UserRepository;
import com.jacent.storefront.security.JwtService;
import com.jacent.storefront.service.AuthService;
import com.jacent.storefront.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JwtConfig jwtConfig, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already in use: {}", request.getEmail());
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .storeId(request.getStoreId())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();
        userRepository.createUser(user);
        log.info("New user registered: email={}", request.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: email={}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail());
        log.info("Login successful: email={}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String email;
        try {
            email = jwtService.extractRefreshTokenUsername(request.getRefreshToken());
        } catch (Exception e) {
            log.warn("Refresh token extraction failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid refresh token");
        }
        User user = userRepository.findByEmail(email);
        if (!jwtService.isRefreshTokenValid(request.getRefreshToken(), user)) {
            log.warn("Refresh token validation failed for: {}", email);
            throw new IllegalArgumentException("Refresh token is expired or invalid");
        }
        log.info("Access token refreshed for: {}", email);
        return buildAuthResponse(user);
    }

    @Override
    public UserResponse getMe() {
        return toUserResponse(SecurityUtils.getCurrentUser());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getAuthExpirationMs())
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .storeId(user.getStoreId())
                .isActive(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
