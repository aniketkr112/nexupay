package com.nexupay.payment.paymentattempt.service.transaction;

import com.nexupay.payment.bank.dto.BankResponse;
import com.nexupay.payment.common.enums.PaymentMethod;
import com.nexupay.payment.common.exception.PaymentAttemptNotFoundException;
import com.nexupay.payment.common.exception.PaymentNotFoundException;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.common.util.PaymentMessages;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptRequest;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptResponse;
import com.nexupay.payment.paymentattempt.entity.PaymentAttempt;
import com.nexupay.payment.paymentattempt.repository.PaymentAttemptRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PaymentAttemptTransactionService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final IdGeneration idGeneration;

    public PaymentAttemptTransactionService(PaymentRepository paymentRepository, PaymentAttemptRepository paymentAttemptRepository, IdGeneration idGeneration) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.idGeneration = idGeneration;
    }

    @Transactional
    public PaymentAttempt preparePaymentAttempt(UpiPaymentAttemptRequest request){
        Payment payment = paymentRepository
                .findByPaymentIdForUpdate(request.getPaymentId())
                .orElseThrow(()->
                        new PaymentNotFoundException(request.getPaymentId()));

        payment.ensurePaymentCanBeAttempted();

        int nextAttemptNumber =paymentAttemptRepository
                .findMaxAttemptNumber(payment)
                .orElse(0)+1;

        PaymentAttempt paymentAttempt =
                new PaymentAttempt(
                        idGeneration.generatePaymentAttemptId(),
                        payment,
                        nextAttemptNumber,
                        PaymentMethod.UPI
                );

        return paymentAttemptRepository.save(paymentAttempt);
    }

    @Transactional
    public PaymentAttempt finalizePaymentAttempt( String attemptId,BankResponse bankResponse){
        PaymentAttempt paymentAttempt =
                paymentAttemptRepository
                        .findByAttemptId(attemptId)
                        .orElseThrow(()-> new PaymentAttemptNotFoundException(attemptId));

        Payment payment = paymentAttempt.getPayment();
        switch (bankResponse.getStatus()) {

            case SUCCESS -> handleSuccess(paymentAttempt,payment,bankResponse);

            case FAILED ->  handleFailure(paymentAttempt,payment,bankResponse);

            case UNKNOWN -> handleUnknown(paymentAttempt,payment,bankResponse);
        };
        return paymentAttempt;
    }

    private void handleSuccess(PaymentAttempt paymentAttempt, Payment payment, BankResponse bankResponse) {
        paymentAttempt.markSuccess(bankResponse.getBankReferenceId());
        payment.markSuccessful();
    }

    private void handleFailure(PaymentAttempt paymentAttempt,Payment payment,BankResponse bankResponse){
        paymentAttempt.markFailed(bankResponse.getFailureReason());
    }

    private void handleUnknown(PaymentAttempt paymentAttempt,Payment payment,BankResponse bankResponse){
        paymentAttempt.markUnknown(bankResponse.getFailureReason());
    }


}
