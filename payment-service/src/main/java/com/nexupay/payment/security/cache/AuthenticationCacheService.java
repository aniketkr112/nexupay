package com.nexupay.payment.security.cache;

import com.nexupay.payment.security.auth.AuthenticatedMerchant;

public interface AuthenticationCacheService {

    AuthenticatedMerchant get(String apiKey, String secretKey);

    void put(String apiKey, String secretKey,
             AuthenticatedMerchant authenticatedMerchant);

    void evict(String apiKey);
}