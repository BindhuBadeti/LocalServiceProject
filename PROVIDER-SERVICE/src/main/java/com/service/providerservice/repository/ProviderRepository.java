package com.service.providerservice.repository;

import com.service.providerservice.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByUserId(String userId);

    boolean existsByUserId(String userId);
}