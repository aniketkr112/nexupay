package com.nexupay.payment.refund.service.transaction;

import com.nexupay.payment.bank.dto.BankRefundRequest;
import com.nexupay.payment.bank.dto.BankRefundSubmissionResponse;
import com.nexupay.payment.bank.enums.BankRefundSubmissionStatus;
import com.nexupay.payment.bank.service.BankService;
import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundProcessingTransactionService {

    private final RefundRepository refundRepository;
    private final BankService bankService;

    @Transactional
    public void processSingleRefund(Refund refund) {

        BankRefundRequest request = new BankRefundRequest(
                refund.getRefundId(),
                refund.getPaymentId(),
                refund.getAmount(),
                refund.getCurrency()
        );

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

        refundRepository.save(refund);
    }
}
