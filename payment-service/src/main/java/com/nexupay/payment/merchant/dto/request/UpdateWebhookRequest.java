package com.nexupay.payment.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateWebhookRequest {

    @NotBlank
    @Size(max = 255)
    private String webhookUrl;
}