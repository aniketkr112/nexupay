package com.nexupay.payment.credential.service;

import com.nexupay.payment.common.cryptography.SecretKeyHasher;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.common.enums.Environment;
import com.nexupay.payment.common.exception.CredentialNotFoundException;
import com.nexupay.payment.common.exception.MerchantNotFoundException;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.credential.dto.request.CreateCredentialRequest;
import com.nexupay.payment.credential.dto.response.CreateCredentialResponse;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.merchant.entity.Merchant;
import com.nexupay.payment.merchant.repository.MerchantRepository;
import com.nexupay.payment.outbox.entity.OutboxEvent;
import com.nexupay.payment.outbox.repository.OutboxEventRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import com.nexupay.payment.security.bloom.SharedApiKeySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CredentialManagementService {

    private final MerchantRepository merchantRepository;
    private final ApiCredentialRepository apiCredentialRepository;
    private final IdGeneration idGeneration;
    private final SecretKeyHasher secretKeyHasher;
    private final OutboxEventRepository outboxEventRepository;
    private final SharedApiKeySetService sharedApiKeySetService;


    @Transactional
    public CreateCredentialResponse createCredential(
            AuthenticatedMerchant authenticatedMerchant,
            CreateCredentialRequest request) {

        Merchant merchant = merchantRepository.findById(authenticatedMerchant.getId())
                .orElseThrow(() -> new MerchantNotFoundException(authenticatedMerchant.getId()));

        Environment internalEnvironment = "TEST".equals(request.getEnvironment())
                ? Environment.SANDBOX
                : Environment.PRODUCTION;

        String credentialId = idGeneration.generateCredentialId();
        String apiKey = idGeneration.generateApiKey(internalEnvironment);
        String secretKey = idGeneration.generateSecretKey(internalEnvironment);
        String secretKeyHash = secretKeyHasher.hash(secretKey);

        ApiCredential credential = new ApiCredential(
                credentialId,
                request.getName(),
                merchant,
                apiKey,
                secretKeyHash,
                internalEnvironment,
                CredentialStatus.ACTIVE
        );

        ApiCredential savedCredential = apiCredentialRepository.save(credential);

        outboxEventRepository.save(new OutboxEvent(
                "API_CREDENTIAL_CREATED",
                "API_CREDENTIAL",
                credentialId,
                apiKey
        ));

        // Register before returning the raw secret, so a Redis failure rolls back
        // the database transaction instead of exposing a credential that cannot
        // be authenticated reliably during Bloom-filter propagation lag.
        sharedApiKeySetService.add(apiKey);

        return new CreateCredentialResponse(
                savedCredential.getCredentialId(),
                savedCredential.getName(),
                savedCredential.getApiKey(),
                secretKey,
                request.getEnvironment(),
                savedCredential.getStatus().name(),
                savedCredential.getCreatedAt()
        );
    }

    /**
     * Revokes a credential owned by the authenticated merchant. Repeating the
     * operation is safe: a credential that is already revoked remains revoked.
     */
    @Transactional
    public void revokeCredential(
            String credentialId,
            AuthenticatedMerchant authenticatedMerchant) {

        ApiCredential credential = apiCredentialRepository
                .findByCredentialIdAndMerchantId(
                        credentialId,
                        authenticatedMerchant.getId()
                )
                .orElseThrow(CredentialNotFoundException::new);

        credential.revoke();
    }
}
