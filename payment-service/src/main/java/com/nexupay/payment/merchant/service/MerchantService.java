package com.nexupay.payment.merchant.service;

import com.nexupay.payment.common.exception.MerchantAlreadyExistsException;
import com.nexupay.payment.common.security.SecretKeyHasher;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.credential.enums.CredentialStatus;
import com.nexupay.payment.credential.enums.Environment;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.merchant.dto.request.CreateMerchantRequest;
import com.nexupay.payment.merchant.dto.response.CreateMerchantResponse;
import com.nexupay.payment.merchant.entity.Merchant;
import com.nexupay.payment.merchant.enums.MerchantStatus;
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
        /*1. Validate request
        2. Check email already exists?
        3. Generate merchant_id
        4. Create Merchant entity
        5. Save Merchant
        6. Generate API Key & Secret Key
        7. Hash Secret Key
        8. Create ApiCredential entity
        9. Save ApiCredential
        10. Return CreateMerchantResponse*/

        merchantRepository.findByEmail(request.getEmail()).ifPresent(merchant->{ throw new MerchantAlreadyExistsException("Merchant already exists with email: "+request.getEmail());});
        String merchantId = idGeneration.generateMerchantId();
        Merchant merchant = new Merchant(merchantId,request.getBusinessName(), request.getEmail(), request.getPhone(), MerchantStatus.ACTIVE);
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
        response.setSecretKey(secretKeyHash);

        return response;
    }
}
