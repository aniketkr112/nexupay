package com.nexupay.payment.bank.dto;

import java.math.BigDecimal;

public class BankRequest {
    private String transactionId;
    private Long amount;
    private String upiId;

    public String getTransactionId() {
        return transactionId;
    }

    public BankRequest(String transactionId, Long amount, String upiId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.upiId = upiId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getUpiId() {
        return upiId;
    }
}
