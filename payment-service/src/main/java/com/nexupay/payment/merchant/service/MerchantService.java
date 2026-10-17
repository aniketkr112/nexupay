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
import com.nexupay.payment.security.bloom.SharedApiKeySetService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantCreationTransactionService merchantCreationTransactionService;
    private final SharedApiKeySetService sharedApiKeySetService;


    @Transactional
    public CreateMerchantResponse createMerchant(CreateMerchantRequest request){
        // The database transaction commits before this returns.
        CreateMerchantResponse response =
                merchantCreationTransactionService.createMerchant(request);

        // Register the key before exposing credentials to the merchant.
        sharedApiKeySetService.add(response.getApiKey());

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
