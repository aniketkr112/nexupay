package com.nexupay.payment.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateWebhookRequest {

    @NotBlank(message = "Webhook URL is required")
    @Size(max = 255, message = "Webhook URL must not exceed 255 characters")
    @Pattern(
            regexp = "^https://.*$",
            message = "Webhook URL must use HTTPS"
    )
    private String webhookUrl;
}