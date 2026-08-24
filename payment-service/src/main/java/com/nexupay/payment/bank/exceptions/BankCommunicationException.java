package com.nexupay.payment.bank.exceptions;

public class BankCommunicationException extends RuntimeException {
    public BankCommunicationException(String message) {
        super(message);
    }
}
