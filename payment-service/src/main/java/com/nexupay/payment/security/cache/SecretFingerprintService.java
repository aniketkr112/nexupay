package com.nexupay.payment.security.cache;

public interface SecretFingerprintService {

    String generate(String secretKey);
}