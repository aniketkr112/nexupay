package com.nexupay.payment.bank.dto;

import java.math.BigDecimal;

public class BankRequest {
    private String transactionId;
    private BigDecimal amount;
    private String upiId;

    public String getTransactionId() {
        return transactionId;
    }

    public BankRequest(String transactionId, BigDecimal amount, String upiId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.upiId = upiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUpiId() {
        return upiId;
    }
}
