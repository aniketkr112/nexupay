package com.nexupay.payment.security.bloom;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ApiKeyBloomFilterServiceImpl implements ApiKeyBloomFilterService{

    private static final int EXPECTED_INSERTIONS = 1_000_000;

    private static final double FALSE_POSITIVE_PROBABILITY = 0.001;

    private final BloomFilter<CharSequence> bloomFilter =
            BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    EXPECTED_INSERTIONS,
                    FALSE_POSITIVE_PROBABILITY
            );

    @Override
    public void add(String apiKey) {

    }

    @Override
    public boolean mightContains(String apiKey) {
        return false;
    }

    @Override
    public void rebuild() {

    }
}
