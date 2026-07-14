package com.nexupay.payment.credential.repository;

import com.nexupay.payment.credential.entity.ApiCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiCredentialRepository extends JpaRepository<ApiCredential,Long> {
    Optional<ApiCredential> findByApiKey(String apiKey);
}
