package com.service.providerservice.service;

import com.service.providerservice.dto.*;
import com.service.providerservice.entity.Provider;
import com.service.providerservice.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository repository;

    // ✅ CREATE / REGISTER PROVIDER
    public ProviderResponse createProvider(String userId, String email, String role, ProviderRequest request) {

        // 🔐 Role check
        if (!"PROVIDER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only PROVIDER can create profile");
        }

        // ❌ Prevent duplicate profile
        if (repository.existsByUserId(userId)) {
            throw new RuntimeException("Provider profile already exists");
        }

        // ✅ Build entity
        Provider provider = Provider.builder()
                .userId(userId)
                .email(email)
                .categories(request.getCategories())
                .experience(request.getExperience())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .basePrice(request.getBasePrice())
                .verified(false)
                .build();

        Provider saved = repository.save(provider);

        return mapToResponse(saved);
    }

    // ✅ GET MY PROFILE
    public ProviderResponse getMyProfile(String userId) {

        Provider provider = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        return mapToResponse(provider);
    }

    // 🔄 MAPPER
    private ProviderResponse mapToResponse(Provider provider) {
        return ProviderResponse.builder()
                .userId(provider.getUserId())
                .email(provider.getEmail())
                .fullName(provider.getFullName())
                .profileImageUrl(provider.getProfileImageUrl())
                .categories(provider.getCategories())
                .experience(provider.getExperience())
                .latitude(provider.getLatitude())
                .longitude(provider.getLongitude())
                .address(provider.getAddress())
                .basePrice(provider.getBasePrice())
                .verified(provider.getVerified())
                .build();
    }
}