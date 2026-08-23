package com.nexupay.payment.bank.service;

import com.nexupay.payment.bank.dto.*;
import com.nexupay.payment.bank.entity.BankRefund;
import com.nexupay.payment.bank.enums.BankRefundLookupStatus;
import com.nexupay.payment.bank.enums.BankRefundStatus;
import com.nexupay.payment.bank.enums.BankRefundSubmissionStatus;
import com.nexupay.payment.bank.enums.BankTransactionStatus;
import com.nexupay.payment.bank.repository.BankRefundRepository;
import com.nexupay.payment.common.util.IdGeneration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MockBankServiceImpl implements BankService{

    private final Map<String, BankResponse> bankTransactions =
            new ConcurrentHashMap<>();

    private final IdGeneration idGeneration;
    private final BankRefundRepository bankRefundRepository;

    @Override
    public BankResponse processPayment(BankRequest request) {

        BankTransactionStatus status =
                determineStatus(request.getUpiId());
        String bankReferencedId = idGeneration.generateBankReferenceId();

        BankResponse response = switch (status) {

            case SUCCESS -> BankResponse.success(bankReferencedId);

            case FAILED,NOT_FOUND -> BankResponse.failed();

            case UNKNOWN -> BankResponse.unknown();
        };
        bankTransactions.put(request.getTransactionId(),response);

        return response;

    }

    @Override
    public BankResponse checkPaymentStatus(String attemptId) {
        BankResponse response = bankTransactions.get(attemptId);
        if(response==null){
            return BankResponse.notFound();
        }
        return response;
    }

    @Override
    public BankRefundSubmissionResponse submitRefund(BankRefundRequest request) {

        Optional<BankRefund> existingRefund =
                bankRefundRepository.findByRefundId(request.getRefundId());

        if (existingRefund.isPresent()) {
            BankRefund refund = existingRefund.get();

            return new BankRefundSubmissionResponse(
                    refund.getRefundId(),
                    refund.getBankReferenceId(),
                    BankRefundSubmissionStatus.SUCCESS
            );
        }

        String bankReferenceId =
                idGeneration.generateBankReferenceId();

       BankRefund bankRefund = BankRefund.create(request,bankReferenceId);

        bankRefundRepository.save(bankRefund);

        return new BankRefundSubmissionResponse(
                request.getRefundId(),
                bankReferenceId,
                BankRefundSubmissionStatus.SUCCESS
        );
    }

    @Override
    public BankRefundLookupResponse lookupRefund(String refundId) {

        Optional<BankRefund> existingRefund =
                bankRefundRepository.findByRefundId(refundId);

        if (existingRefund.isEmpty()) {
            return new BankRefundLookupResponse(
                    refundId,
                    null,
                    BankRefundLookupStatus.NOT_FOUND
            );
        }

        BankRefund refund = existingRefund.get();

        return new BankRefundLookupResponse(
                refund.getRefundId(),
                refund.getBankReferenceId(),
                refund.getStatus() == BankRefundStatus.SUCCESS
                        ? BankRefundLookupStatus.SUCCESS
                        : BankRefundLookupStatus.FAILED
        );
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
