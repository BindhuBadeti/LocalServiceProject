package com.service.authservice.controller;

import com.service.authservice.dto.*;
import com.service.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<?> verifyOtp(@RequestBody OtpRequest request) {
        return authService.verifyOtp(request);
    }

    // 🔁 Forgot password (NO OTP)
    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody ResetPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }

        String token = authHeader.substring(7);

        return authService.logout(token);
    }

    @PostMapping("/refresh-token")
    public ApiResponse<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }
    
 // ✅ GET USER BY ID
    @GetMapping("/users/{id}")
    public ApiResponse<?> getUserById(@PathVariable Long id) {
        return authService.getUserById(id);
    }

    // ✅ UPDATE USER BY ID
    @PutMapping("/users/{id}")
    public ApiResponse<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        return authService.updateUser(id, request);
    }

    // ✅ DELETE USER BY ID
    @DeleteMapping("/users/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        return authService.deleteUser(id);
    }
    
 // ✅ GET ALL USERS
    @GetMapping("/users")
    public ApiResponse<?> getAllUsers() {
        return authService.getAllUsers();
    }
    // ✅ LOGOUT
//    @PostMapping("/logout")
//    public ApiResponse<?> logout(HttpServletRequest request) {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new RuntimeException("Token missing");
//        }
//
//        String token = authHeader.substring(7);
//
//        return authService.logout(token);
//    }

}