package com.service.providerservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderRequest {

    // 👤 Personal Info
    @NotBlank
    private String fullName;

    // 🔧 Service Details
    @NotEmpty
    private List<String> categories;

    @NotBlank
    private String experience;

    // 📍 Location
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String address;

    // 💰 Pricing (optional)
    private Double basePrice;
}