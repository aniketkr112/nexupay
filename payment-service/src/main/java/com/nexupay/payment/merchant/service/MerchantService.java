package com.nexupay.payment.merchant.service;

import com.nexupay.payment.common.exception.MerchantAlreadyExistsException;
import com.nexupay.payment.common.cryptography.SecretKeyHasher;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.common.enums.Environment;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.merchant.dto.request.CreateMerchantRequest;
import com.nexupay.payment.merchant.dto.response.CreateMerchantResponse;
import com.nexupay.payment.merchant.entity.Merchant;
import com.nexupay.payment.common.enums.MerchantStatus;
import com.nexupay.payment.merchant.repository.MerchantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final ApiCredentialRepository apiCredentialRepository;
    private final IdGeneration idGeneration;
    private final SecretKeyHasher secretKeyHasher;


    public MerchantService(MerchantRepository merchantRepository, ApiCredentialRepository apiCredentialRepository, IdGeneration idGeneration, SecretKeyHasher secretKeyHasher) {
        this.merchantRepository = merchantRepository;
        this.apiCredentialRepository = apiCredentialRepository;
        this.idGeneration = idGeneration;
        this.secretKeyHasher = secretKeyHasher;
    }

    @Transactional
    public CreateMerchantResponse createMerchant(CreateMerchantRequest request){

        merchantRepository.findByEmail(request.getEmail()).ifPresent(merchant->{ throw new MerchantAlreadyExistsException("Merchant already exists with email: "+request.getEmail());});
        String merchantId = idGeneration.generateMerchantId();
        Merchant merchant = Merchant.create(merchantId,request);
        Merchant savedMerchant = merchantRepository.save(merchant);

        Environment environment = Environment.SANDBOX;
        String apiKey = idGeneration.generateApiKey(environment);
        String secretKey = idGeneration.generateSecretKey(environment);
        String secretKeyHash = secretKeyHasher.hash(secretKey);
        String apiCredentialId = idGeneration.generateCredentialId();

        ApiCredential apiCredential =  new ApiCredential(apiCredentialId,savedMerchant,apiKey,secretKeyHash,environment, CredentialStatus.ACTIVE);
        apiCredentialRepository.save(apiCredential);

        CreateMerchantResponse response = new CreateMerchantResponse();
        response.setMerchantId(merchantId);
        response.setApiKey(apiKey);
        response.setSecretKey(secretKey);

        return response;
    }
}
