package com.service.providerservice.controller;

import com.service.providerservice.dto.*;
import com.service.providerservice.service.ProviderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    // ✅ REGISTER PROVIDER
    @PostMapping("/register")
    public ProviderResponse createProvider(
            HttpServletRequest request,
            @Valid @RequestBody ProviderRequest providerRequest
    ) {

        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");
        System.out.println("USER ID: " + userId);
        System.out.println("EMAIL: " + email);
        System.out.println("ROLE: " + role);

        return providerService.createProvider(userId, email, role, providerRequest);
    }

    // ✅ GET MY PROFILE
    @GetMapping("/me")
    public ProviderResponse getMyProfile(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        return providerService.getMyProfile(userId);
    }
}