package com.nexupay.payment.security.bloom;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class ApiKeyBloomFilterServiceImpl implements ApiKeyBloomFilterService{

    private static final int EXPECTED_INSERTIONS = 1_000_000;

    private static final double FALSE_POSITIVE_PROBABILITY = 0.001;

    private final ApiCredentialRepository apiCredentialRepository;

    public ApiKeyBloomFilterServiceImpl(
            ApiCredentialRepository apiCredentialRepository
    ) {
        this.apiCredentialRepository = apiCredentialRepository;
    }

    @PostConstruct
    public void initialize() {
        rebuild();
    }

    private final BloomFilter<CharSequence> bloomFilter =
            BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    EXPECTED_INSERTIONS,
                    FALSE_POSITIVE_PROBABILITY
            );

    @Override
    public synchronized void add(String apiKey) {
        bloomFilter.put(apiKey);
        log.info(
                "Newly created api key added to bloom: apiKey={}",
                apiKey
        );
    }

    @Override
    public synchronized boolean mightContains(String apiKey) {
        return bloomFilter.mightContain(apiKey);
    }




    @Override
    public void rebuild() {
        List<ApiCredential> credentials =
                apiCredentialRepository.findAllByStatus(
                        CredentialStatus.ACTIVE
                );

        for (ApiCredential credential : credentials) {
            bloomFilter.put(credential.getApiKey());
            log.info(
                    "Api key added to bloom while restart the server: apiKey={}",credential.getApiKey()
            );
        }
    }
}
