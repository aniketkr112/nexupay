package com.nexupay.payment.merchant.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMerchantResponse {
    private String merchantId;
    private String credentialId;
    private String apiKey;
    private String secretKey;

    public CreateMerchantResponse() {
    }
}
