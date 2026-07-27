package com.nexupay.payment.bank.dto;

import com.nexupay.payment.common.enums.BankFailureReason;
import com.nexupay.payment.common.enums.BankTransactionStatus;
import com.nexupay.payment.payment.dto.request.PaymentRequest;
import com.nexupay.payment.payment.entity.Payment;

import java.time.LocalDateTime;

public class BankResponse {

    private BankTransactionStatus status;

    private String bankReferenceId;

    private BankFailureReason failureReason;

    public static BankResponse success(String bankReferenceId){
        BankResponse response = new BankResponse();
        response.status = BankTransactionStatus.SUCCESS;
        response.bankReferenceId = bankReferenceId;
        return response;
    }

    public static BankResponse failed(){
        BankResponse response = new BankResponse();
        response.status = BankTransactionStatus.FAILED;
        response.failureReason = BankFailureReason.INSUFFICIENT_BALANCE;
        return response;
    }
    public static BankResponse unknown(){
        BankResponse response = new BankResponse();
        response.status = BankTransactionStatus.UNKNOWN;
        response.failureReason = BankFailureReason.NETWORK_ERROR;
        return response;
    }


    public BankTransactionStatus getStatus() {
        return status;
    }

    public String getBankReferenceId() {
        return bankReferenceId;
    }

    public BankFailureReason getFailureReason() {
        return failureReason;
    }
}
