package com.nexupay.payment.security.auth;

public class AuthenticatedMerchant {
    private Long merchantId;
    private String merchantPublicId;

    public AuthenticatedMerchant(Long merchantId, String merchantPublicId) {
        this.merchantId = merchantId;
        this.merchantPublicId = merchantPublicId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getMerchantPublicId() {
        return merchantPublicId;
    }
}
