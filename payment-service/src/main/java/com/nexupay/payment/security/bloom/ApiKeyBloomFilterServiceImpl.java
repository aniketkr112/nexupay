package com.nexupay.payment.security.bloom;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.nexupay.payment.common.enums.CredentialStatus;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
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
        }
    }
}
