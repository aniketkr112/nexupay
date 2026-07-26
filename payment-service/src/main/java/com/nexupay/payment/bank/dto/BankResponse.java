package com.nexupay.payment.bank.dto;

import com.nexupay.payment.common.enums.BankFailureReason;
import com.nexupay.payment.common.enums.BankTransactionStatus;

public class BankResponse {

    private BankTransactionStatus status;

    private String bankReferenceId;

    private BankFailureReason failureReason;

    public BankTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(BankTransactionStatus status) {
        this.status = status;
    }

    public String getBankReferenceId() {
        return bankReferenceId;
    }

    public void setBankReferenceId(String bankReferenceId) {
        this.bankReferenceId = bankReferenceId;
    }

    public BankFailureReason getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(BankFailureReason failureReason) {
        this.failureReason = failureReason;
    }
}
