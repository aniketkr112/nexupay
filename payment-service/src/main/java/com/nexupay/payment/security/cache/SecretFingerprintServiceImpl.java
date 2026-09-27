package com.nexupay.payment.security.cache;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class SecretFingerprintServiceImpl implements SecretFingerprintService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] signingKey;

    public SecretFingerprintServiceImpl(
            @Value("${nexupay.security.cache-key}") String cacheKey) {

        this.signingKey = cacheKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String generate(String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(signingKey, HMAC_ALGORITHM);

            mac.init(secretKeySpec);

            byte[] digest =
                    mac.doFinal(secretKey.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to generate secret fingerprint",
                    exception
            );
        }
    }
}