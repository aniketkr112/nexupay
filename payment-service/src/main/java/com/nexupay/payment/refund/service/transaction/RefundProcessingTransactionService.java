package com.nexupay.payment.refund.service.transaction;

import com.nexupay.payment.bank.dto.BankRefundLookupResponse;
import com.nexupay.payment.bank.dto.BankRefundRequest;
import com.nexupay.payment.bank.dto.BankRefundSubmissionResponse;
import com.nexupay.payment.bank.enums.BankRefundSubmissionStatus;
import com.nexupay.payment.bank.exceptions.BankCommunicationException;
import com.nexupay.payment.bank.service.BankService;
import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundProcessingTransactionService {

    private final RefundRepository refundRepository;
    private final BankService bankService;

    @Transactional
    public void processSingleRefund(Refund refund) {

        if (refund.isBankSubmissionAttempted()) {
            recoverRefund(refund);
            return;
        }

        submitNewRefund(refund);
    }

    private void submitNewRefund(Refund refund) {

        log.info(
                "RefundId {} going to submit bank",
                refund.getRefundId()
        );
        BankRefundRequest request = new BankRefundRequest(
                refund.getRefundId(),
                refund.getPaymentId(),
                refund.getAmount(),
                refund.getCurrency()
        );

        try {

            BankRefundSubmissionResponse response =
                    bankService.submitRefund(request);

            if (response.getStatus() ==
                    BankRefundSubmissionStatus.SUCCESS) {

                refund.markSuccessful(
                        response.getBankReferenceId()
                );

            } else {
                refund.markFailed();
            }
            refund.clearProcessing();
            refundRepository.save(refund);

        } catch (BankCommunicationException ex) {

            refund.markBankSubmissionAttempted();
            refund.clearProcessing();
            refundRepository.save(refund);

            log.error(
                    "Refund submission outcome is uncertain for {}.",
                    refund.getRefundId(),
                    ex
            );
        }
    }

    private void recoverRefund(Refund refund) {

        log.info(
                "Refund recovery lookup for refundId {}",
                refund.getRefundId()
        );
        try {

            BankRefundLookupResponse response =
                    bankService.lookupRefund(refund.getRefundId());

            log.info(
                    "Refund lookup for refundId {} with bank status {}",
                    refund.getRefundId(),response.getStatus()
            );
            switch (response.getStatus()) {

                case SUCCESS -> refund.markSuccessful(
                        response.getBankReferenceId()
                );

                case FAILED -> refund.markFailed();

                case NOT_FOUND -> submitNewRefund(refund);
            }
            refund.clearProcessing();
            refundRepository.save(refund);

        } catch (BankCommunicationException ex) {
            // We still don't know what happened at the bank.
            // Keep the refund PENDING and keep bankSubmissionAttempted = true.
            log.error(
                    "Refund recovery lookup failed for {}",
                    refund.getRefundId(),
                    ex
            );
        }
    }
}
