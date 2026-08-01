package com.nexupay.payment.bank.service;

import com.nexupay.payment.bank.dto.BankRequest;
import com.nexupay.payment.bank.dto.BankResponse;
import com.nexupay.payment.common.enums.BankTransactionStatus;
import com.nexupay.payment.common.util.IdGeneration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MockBankServiceImpl implements BankService{

    private final Map<String, BankResponse> bankTransactions =
            new ConcurrentHashMap<>();

    private final IdGeneration idGeneration;

    @Override
    public BankResponse processPayment(BankRequest request) {

        BankTransactionStatus status =
                determineStatus(request.getUpiId());
        String bankReferencedId = idGeneration.generateBankReferenceId();

        BankResponse response = switch (status) {

            case SUCCESS -> BankResponse.success(bankReferencedId);

            case FAILED -> BankResponse.failed();

            case UNKNOWN -> BankResponse.unknown();

            case NOT_FOUND -> BankResponse.notFound();
        };

        bankTransactions.put(request.getTransactionId(),response);

        return response;

    }

    @Override
    public BankResponse checkPaymentStatus(String attemptId) {
        return bankTransactions.get(attemptId);
    }

    private BankTransactionStatus determineStatus(String upiId) {

        return switch (upiId.toLowerCase()) {

            case "success@upi" -> BankTransactionStatus.SUCCESS;

            case "failed@upi" -> BankTransactionStatus.FAILED;

            case "unknown@upi" -> BankTransactionStatus.UNKNOWN;

            default -> BankTransactionStatus.FAILED;
        };
    }
}
