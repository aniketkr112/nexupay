package com.nexupay.payment.common.enums;

public enum CheckoutStatus {
    PAYABLE("Choose a payment method."),

    EXPIRED("This payment has expired."),

    COMPLETED("Payment completed successfully."),

    CANCELLED("Payment cancelled");

    CheckoutStatus(String message) {
        this.message = message;
    }

    private final String message;

    public String getMessage() {
        return message;
    }
}
