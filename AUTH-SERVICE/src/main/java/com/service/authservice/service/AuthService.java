package com.service.authservice.service;

import com.service.authservice.config.JwtService;
import com.service.authservice.dto.*;
import com.service.authservice.entity.*;
import com.service.authservice.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // OTP store (static for now)
    private final Map<String, String> otpStore = new HashMap<>();

    // ================= REGISTER =================
    public ApiResponse<?> register(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .verified(false)
                .profileCompleted(false)
                .build();

        userRepository.save(user);

        user.setPassword(null);

        return buildResponse(true, "User registered successfully", user);
    }

    // ================= LOGIN =================
    public ApiResponse<?> login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // ✅ STATIC OTP (as you requested)
        String otp = "123456";
        otpStore.put(user.getEmail(), otp);

        return buildResponse(true, "OTP sent successfully (use 123456)", null);
    }

    @Transactional
    // ================= VERIFY OTP =================
    public ApiResponse<?> verifyOtp(OtpRequest request) {

        String storedOtp = otpStore.get(request.getEmail());

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerified(true);
        userRepository.save(user);

        otpStore.remove(request.getEmail());

        // ✅ UPDATED JWT (userId + email + role)
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtService.generateRefreshToken(
                user.getId()
        );

        // Remove old refresh tokens
        refreshTokenRepository.deleteByUser(user);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build()
        );

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .verified(true)
                .profileCompleted(user.isProfileCompleted())
                .build();

        return buildResponse(true, "Login successful", response);
    }

    // ================= FORGOT PASSWORD =================
    public ApiResponse<?> forgotPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return buildResponse(true, "Password updated successfully. Please login.", null);
    }

    // ================= LOGOUT =================
    public ApiResponse<?> logout(String token) {

        blacklistRepo.save(
                BlacklistedToken.builder()
                        .token(token)
                        .blacklistedAt(LocalDateTime.now())
                        .build()
        );

        return buildResponse(true, "Logged out successfully", null);
    }

    // ================= REFRESH TOKEN =================
    public ApiResponse<?> refreshToken(RefreshTokenRequest request) {

        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            return buildResponse(false, "Refresh token expired", null);
        }

        User user = stored.getUser();

        // ✅ UPDATED JWT AGAIN
        String newAccessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .role(user.getRole().name())
                .verified(true)
                .profileCompleted(user.isProfileCompleted())
                .build();

        return buildResponse(true, "Token refreshed", response);
    }

    // ================= COMMON =================
    private <T> ApiResponse<T> buildResponse(boolean success, String message, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
    
    
    
    public ApiResponse<?> getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(null);

        return buildResponse(true, "User fetched successfully", user);
    }
    
    
    public ApiResponse<?> updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);

        user.setPassword(null);

        return buildResponse(true, "User updated successfully", user);
    }
    
    
    
    public ApiResponse<?> deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);

        return buildResponse(true, "User deleted successfully", null);
    }
    
    public ApiResponse<?> getAllUsers() {

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // ❌ NEVER expose passwords
        users.forEach(user -> user.setPassword(null));

        return buildResponse(true, "Users fetched successfully", users);
    }
    
    
//    public ApiResponse<?> logout(String token) {
//
//        blacklistRepo.save(
//                BlacklistedToken.builder()
//                        .token(token)
//                        .blacklistedAt(LocalDateTime.now())
//                        .build()
//        );
//
//        return buildResponse(true, "Logged out successfully", null);
//    }
}