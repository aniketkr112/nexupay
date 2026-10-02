package com.nexupay.payment.security.bloom;

public interface ApiKeyBloomFilterService {

    void add(String apiKey);
    boolean mightContains(String apiKey);
    void rebuild();
}
