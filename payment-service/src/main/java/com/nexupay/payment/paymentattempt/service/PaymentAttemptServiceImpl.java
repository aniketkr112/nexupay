package com.nexupay.payment.paymentattempt.service;

import com.nexupay.payment.bank.dto.BankRequest;
import com.nexupay.payment.bank.dto.BankResponse;
import com.nexupay.payment.bank.service.BankService;
import com.nexupay.payment.common.util.PaymentMessages;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptResponse;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptRequest;
import com.nexupay.payment.paymentattempt.entity.PaymentAttempt;
import com.nexupay.payment.paymentattempt.service.transaction.PaymentAttemptTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PaymentAttemptServiceImpl implements PaymentAttemptService{

    private final BankService bankService;
    private final PaymentAttemptTransactionService paymentAttemptTransactionService;


    @Override
    public UpiPaymentAttemptResponse createUpiPaymentAttempt(UpiPaymentAttemptRequest request) {

        PaymentAttempt paymentAttempt =
                paymentAttemptTransactionService
                        .preparePaymentAttempt(request);

        BankRequest bankRequest =
                createBankRequest(
                        paymentAttempt,
                        paymentAttempt.getPayment(),
                        request);

        BankResponse bankResponse =
                bankService.processPayment(bankRequest);

        paymentAttempt = paymentAttemptTransactionService
                .finalizePaymentAttempt(
                        paymentAttempt.getAttemptId(),
                        bankResponse);

        return toResponse(paymentAttempt,bankResponse);
    }

    private BankRequest createBankRequest(PaymentAttempt paymentAttempt,Payment payment,UpiPaymentAttemptRequest request){
        return new BankRequest(paymentAttempt.getAttemptId(),payment.getAmount(),request.getUpiId());
    }
    private UpiPaymentAttemptResponse toResponse(
            PaymentAttempt paymentAttempt,
            BankResponse bankResponse) {

        Payment payment = paymentAttempt.getPayment();

        String message = switch (bankResponse.getStatus()) {

            case SUCCESS -> PaymentMessages.success();

            case FAILED,NOT_FOUND -> PaymentMessages.failed(bankResponse.getFailureReason());

            case UNKNOWN -> PaymentMessages.unknown();
        };

        return new UpiPaymentAttemptResponse(
                payment.getPaymentId(),
                paymentAttempt.getAttemptId(),
                payment.getStatus(),
                message
        );
    }

}
