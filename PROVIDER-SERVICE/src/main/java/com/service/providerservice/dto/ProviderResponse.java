package com.service.providerservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResponse {

    private String userId;
    private String email;

    private String fullName;
    private String profileImageUrl;

    private List<String> categories;
    private String experience;

    private Double latitude;
    private Double longitude;
    private String address;

    private Double basePrice;

    private Boolean verified;
}