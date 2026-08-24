package com.nexupay.payment.bank.service;

import com.nexupay.payment.bank.dto.*;
import com.nexupay.payment.bank.entity.BankRefund;
import com.nexupay.payment.bank.enums.*;
import com.nexupay.payment.bank.exceptions.BankCommunicationException;
import com.nexupay.payment.bank.repository.BankRefundRepository;
import com.nexupay.payment.common.util.IdGeneration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private RefundSimulationMode refundSimulationMode =
            RefundSimulationMode.NORMAL_SUCCESS;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BankRefundSubmissionResponse submitRefund(BankRefundRequest request) {

        Optional<BankRefund> existingRefund =
                bankRefundRepository.findByRefundId(request.getRefundId());

        if (existingRefund.isPresent()) {
            BankRefund refund = existingRefund.get();
            return new BankRefundSubmissionResponse(
                    refund.getRefundId(),
                    refund.getBankReferenceId(),
                    refund.getStatus() == BankRefundStatus.SUCCESS
                            ? BankRefundSubmissionStatus.SUCCESS
                            : BankRefundSubmissionStatus.FAILED
            );
        }

        if (refundSimulationMode ==
                RefundSimulationMode.REQUEST_NOT_REACHED) {

            throw new BankCommunicationException(
                    "Simulated request did not reach bank"
            );
        }

        String bankReferenceId =
                idGeneration.generateBankReferenceId();

        BankRefundStatus bankStatus =
                refundSimulationMode ==
                        RefundSimulationMode.RESPONSE_LOST_FAILED
                        || refundSimulationMode ==
                        RefundSimulationMode.FAILED
                        ? BankRefundStatus.FAILED
                        : BankRefundStatus.SUCCESS;

        BankRefund bankRefund =
                BankRefund.create(
                        request,
                        bankReferenceId,
                        bankStatus
                );

        bankRefundRepository.save(bankRefund);

        if (refundSimulationMode ==
                RefundSimulationMode.RESPONSE_LOST_SUCCESS
                || refundSimulationMode ==
                RefundSimulationMode.RESPONSE_LOST_FAILED) {

            throw new BankCommunicationException(
                    "Simulated lost bank response"
            );
        }

        return new BankRefundSubmissionResponse(
                request.getRefundId(),
                bankReferenceId,
                bankStatus == BankRefundStatus.SUCCESS
                        ? BankRefundSubmissionStatus.SUCCESS
                        : BankRefundSubmissionStatus.FAILED
        );
    }

    @Override
    public BankRefundLookupResponse lookupRefund(String refundId) {

        if (refundSimulationMode == RefundSimulationMode.LOOKUP_FAILURE) {
            throw new BankCommunicationException(
                    "Simulated lookup communication failure"
            );
        }

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

    public void setRefundSimulationMode(RefundSimulationMode mode) {
        this.refundSimulationMode = mode;
    }

}
