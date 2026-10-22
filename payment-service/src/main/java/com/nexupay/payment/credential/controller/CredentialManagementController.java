package com.nexupay.payment.credential.controller;

import com.nexupay.payment.credential.dto.request.CreateCredentialRequest;
import com.nexupay.payment.credential.dto.response.CreateCredentialResponse;
import com.nexupay.payment.credential.service.CredentialManagementService;
import com.nexupay.payment.security.CurrentMerchantProvider;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants/credentials")
@RequiredArgsConstructor
public class CredentialManagementController {

    private final CredentialManagementService credentialManagementService;
    private final CurrentMerchantProvider currentMerchantProvider;

    @PostMapping
    public ResponseEntity<CreateCredentialResponse> createCredential(
            @Valid @RequestBody CreateCredentialRequest request) {

        AuthenticatedMerchant authenticatedMerchant =
                currentMerchantProvider.getCurrentMerchant();

        CreateCredentialResponse response = credentialManagementService.createCredential(
                authenticatedMerchant,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/{credentialId}/revoke")
    public ResponseEntity<Void> revokeCredential(
            @PathVariable String credentialId) {

        AuthenticatedMerchant merchant =
                currentMerchantProvider.getCurrentMerchant();

        credentialManagementService.revokeCredential(credentialId, merchant);

        return ResponseEntity.noContent().build();
    }
}
