package com.nexupay.payment.common.util;

import com.nexupay.payment.bank.enums.BankFailureReason;

public final class PaymentMessages {

    public static String success() {
        return "Payment completed successfully.";
    }

    public static String failed(BankFailureReason reason) {
        return switch (reason) {
            case INSUFFICIENT_BALANCE ->
                    "Insufficient balance.";
            case INVALID_UPI ->
                    "Invalid UPI ID.";
            case LIMIT_EXCEEDED ->
                    "Transaction limit exceeded.";
            case NETWORK_ERROR ->
                    "Bank network error. Please try again.";
            default ->
                    "Payment failed.";
        };
    }

    public static String unknown() {
        return "Payment status is pending verification.";
    }
}