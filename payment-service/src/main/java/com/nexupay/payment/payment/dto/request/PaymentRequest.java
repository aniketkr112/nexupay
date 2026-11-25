package com.nexupay.payment.payment.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentRequest {
    @NotBlank(message = "Merchant order id is required")
    @Size(max = 50, message = "Merchant order id must not exceed 50 characters")
    private String merchantOrderId;
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;
    @Email(message = "Customer email must be valid")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    private String customerEmail;
    @Pattern(regexp = "^[0-9]{10}$", message = "Customer phone must contain exactly 10 digits")
    private String customerPhone;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase code")
    private String currency;


    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public void setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
