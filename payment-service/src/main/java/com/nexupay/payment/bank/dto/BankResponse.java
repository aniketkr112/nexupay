package com.nexupay.payment.bank.dto;

import com.nexupay.payment.bank.enums.BankFailureReason;
import com.nexupay.payment.bank.enums.BankTransactionStatus;
import lombok.Getter;


@Getter
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

    public static BankResponse notFound(){
        BankResponse response = new BankResponse();
        response.status = BankTransactionStatus.NOT_FOUND;
        response.failureReason = BankFailureReason.TRANSACTION_NOT_FOUND;
        return response;
    }

}
