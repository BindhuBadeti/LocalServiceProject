package com.service.providerservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Link with Auth Service
    @Column(nullable = false, unique = true)
    private String userId;

    private String email;

    // 👤 Personal Info
    private String fullName;
    private String profileImageUrl;

    // 🔧 Service Details
    @ElementCollection
    @CollectionTable(name = "provider_categories", joinColumns = @JoinColumn(name = "provider_id"))
    @Column(name = "category")
    private List<String> categories;

    private String experience; // e.g. "2 years"

    // 📍 Location
    private Double latitude;
    private Double longitude;
    private String address;

    // 💰 Pricing
    private Double basePrice;

    // ✅ Status
    private Boolean verified = false;
}