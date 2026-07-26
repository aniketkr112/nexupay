package com.nexupay.payment.bank.service;

import com.nexupay.payment.bank.dto.BankRequest;
import com.nexupay.payment.bank.dto.BankResponse;
import com.nexupay.payment.common.enums.BankFailureReason;
import com.nexupay.payment.common.enums.BankTransactionStatus;
import com.nexupay.payment.common.util.IdGeneration;
import org.springframework.stereotype.Service;

@Service
public class MockBankServiceImpl implements BankService{

    private final IdGeneration idGeneration;

    public MockBankServiceImpl(IdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    @Override
    public BankResponse processPayment(BankRequest request) {

        BankTransactionStatus status =
                determineStatus(request.getUpiId());

        return switch (status) {

            case SUCCESS -> successResponse();

            case FAILED -> failedResponse();

            case UNKNOWN -> unknownResponse();
        };

    }

    private BankTransactionStatus determineStatus(String upiId) {

        return switch (upiId.toLowerCase()) {

            case "success@upi" -> BankTransactionStatus.SUCCESS;

            case "failed@upi" -> BankTransactionStatus.FAILED;

            case "unknown@upi" -> BankTransactionStatus.UNKNOWN;

            default -> BankTransactionStatus.FAILED;
        };
    }

    private BankResponse successResponse(){
        BankResponse response = new BankResponse();

        response.setStatus(BankTransactionStatus.SUCCESS);

        response.setBankReferenceId(
                idGeneration.generateBankReferenceId());

        return response;
    }
    private BankResponse failedResponse(){
        BankResponse response = new BankResponse();

        response.setStatus(BankTransactionStatus.FAILED);

        response.setFailureReason(
                BankFailureReason.INSUFFICIENT_BALANCE);

        return response;
    }
    private BankResponse unknownResponse(){
        BankResponse response = new BankResponse();

        response.setStatus(BankTransactionStatus.UNKNOWN);

        response.setFailureReason(
                BankFailureReason.NETWORK_ERROR);

        return response;
    }
}
