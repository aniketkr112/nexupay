package com.nexupay.payment.security.auth;

public class AuthenticatedMerchant {
    private Long id;
    private String merchantPublicId;

    public AuthenticatedMerchant(Long id, String merchantPublicId) {
        this.id = id;
        this.merchantPublicId = merchantPublicId;
    }

    public Long getId() {
        return id;
    }

    public String getMerchantPublicId() {
        return merchantPublicId;
    }
}
