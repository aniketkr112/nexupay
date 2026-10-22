package com.nexupay.payment.common.exception;

public class CredentialNotFoundException extends RuntimeException {
    public CredentialNotFoundException() {
        super("Credential not found");
    }
}
