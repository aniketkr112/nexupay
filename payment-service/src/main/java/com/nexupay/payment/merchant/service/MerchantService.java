package com.nexupay.payment.merchant.service;

import com.nexupay.payment.common.exception.MerchantAlreadyExistsException;
import com.nexupay.payment.common.cryptography.SecretKeyHasher;
import com.nexupay.payment.common.exception.MerchantNotFoundException;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.common.enums.Environment;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.merchant.dto.request.CreateMerchantRequest;
import com.nexupay.payment.merchant.dto.request.UpdateWebhookRequest;
import com.nexupay.payment.merchant.dto.response.CreateMerchantResponse;
import com.nexupay.payment.merchant.entity.Merchant;
import com.nexupay.payment.common.enums.MerchantStatus;
import com.nexupay.payment.merchant.repository.MerchantRepository;
import com.nexupay.payment.outbox.entity.OutboxEvent;
import com.nexupay.payment.outbox.repository.OutboxEventRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final ApiCredentialRepository apiCredentialRepository;
    private final IdGeneration idGeneration;
    private final SecretKeyHasher secretKeyHasher;
    private final OutboxEventRepository outboxEventRepository;


    @Transactional
    public CreateMerchantResponse createMerchant(CreateMerchantRequest request){

        merchantRepository
                .findByEmail(request.getEmail())
                .ifPresent(merchant->{
                            throw new MerchantAlreadyExistsException("Merchant already exists with email: "+request.getEmail());
                        });
        String merchantId = idGeneration.generateMerchantId();
        Merchant merchant = Merchant.create(merchantId,request);
        Merchant savedMerchant = merchantRepository.save(merchant);

        Environment environment = Environment.SANDBOX;
        String apiKey = idGeneration.generateApiKey(environment);
        String secretKey = idGeneration.generateSecretKey(environment);
        String secretKeyHash = secretKeyHasher.hash(secretKey);
        String apiCredentialId = idGeneration.generateCredentialId();

        ApiCredential apiCredential =  new ApiCredential(
                apiCredentialId,
                savedMerchant,
                apiKey,
                secretKeyHash,
                environment,
                CredentialStatus.ACTIVE
        );
        apiCredentialRepository.save(apiCredential);

        OutboxEvent outboxEvent = new OutboxEvent(
                "API_CREDENTIAL_CREATED",
                "API_CREDENTIAL",
                apiCredentialId,
                apiKey
        );

        outboxEventRepository.save(outboxEvent);

        CreateMerchantResponse response = new CreateMerchantResponse();
        response.setMerchantId(merchantId);
        response.setApiKey(apiKey);
        response.setSecretKey(secretKey);

        return response;
    }

    @Transactional
    public void updateWebhookUrl(
            AuthenticatedMerchant authenticatedMerchant,
            UpdateWebhookRequest request) {

        Merchant merchant = merchantRepository
                .findById(authenticatedMerchant.getId())
                .orElseThrow(() ->
                        new MerchantNotFoundException(
                                authenticatedMerchant.getId()
                        )
                );

        merchant.updateWebhookUrl(request.getWebhookUrl());
    }
}
