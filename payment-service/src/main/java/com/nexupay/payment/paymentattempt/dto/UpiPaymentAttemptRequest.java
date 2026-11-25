package com.nexupay.payment.paymentattempt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpiPaymentAttemptRequest {
    @NotBlank(message = "Payment id is required")
    @Size(max = 50, message = "Payment id must not exceed 50 characters")
    private String paymentId;

    @NotBlank(message = "UPI ID is required")
    @Size(max = 255, message = "UPI ID must not exceed 255 characters")
    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+$",
            message = "UPI ID must be in a valid format"
    )
    private String upiId;

    public UpiPaymentAttemptRequest() {
    }

}
