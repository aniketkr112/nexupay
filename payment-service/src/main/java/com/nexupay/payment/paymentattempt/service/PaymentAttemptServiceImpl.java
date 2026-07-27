package com.nexupay.payment.paymentattempt.service;

import com.nexupay.payment.bank.dto.BankRequest;
import com.nexupay.payment.bank.dto.BankResponse;
import com.nexupay.payment.bank.service.BankService;
import com.nexupay.payment.common.enums.PaymentMethod;
import com.nexupay.payment.common.exception.PaymentNotFoundException;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.common.util.PaymentMessages;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptResponse;
import com.nexupay.payment.paymentattempt.dto.UpiPaymentAttemptRequest;
import com.nexupay.payment.paymentattempt.entity.PaymentAttempt;
import com.nexupay.payment.paymentattempt.repository.PaymentAttemptRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class PaymentAttemptServiceImpl implements PaymentAttemptService{

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final IdGeneration idGeneration;
    private final BankService bankService;

    public PaymentAttemptServiceImpl(
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository, IdGeneration idGeneration, BankService bankService) {

        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.idGeneration = idGeneration;
        this.bankService = bankService;
    }

    @Override
    public UpiPaymentAttemptResponse createUpiPaymentAttempt(UpiPaymentAttemptRequest request) {
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

        PaymentAttempt savedPaymentAttempt = paymentAttemptRepository.save(paymentAttempt);

        BankRequest bankRequest = createBankRequest(payment,savedPaymentAttempt,request);
        BankResponse bankResponse =
                bankService.processPayment(bankRequest);

        return switch (bankResponse.getStatus()) {

            case SUCCESS -> handleSuccess(savedPaymentAttempt,payment,bankResponse);

            case FAILED ->  handleFailure(savedPaymentAttempt,payment,bankResponse);

            case UNKNOWN -> handleUnknown(savedPaymentAttempt,payment,bankResponse);
        };
    }

    private BankRequest createBankRequest(Payment payment,PaymentAttempt paymentAttempt,UpiPaymentAttemptRequest request){
        return new BankRequest(paymentAttempt.getAttemptId(),payment.getAmount(),request.getUpiId());
    }
    private UpiPaymentAttemptResponse handleSuccess(PaymentAttempt paymentAttempt,Payment payment,BankResponse bankResponse) {
        paymentAttempt.markSuccess(bankResponse.getBankReferenceId());
        payment.markSuccessful();
        return toUpiPaymentAttemptResponse(payment,paymentAttempt,PaymentMessages.success());
    }

    private UpiPaymentAttemptResponse handleFailure(PaymentAttempt paymentAttempt,Payment payment,BankResponse bankResponse){
        paymentAttempt.markFailed(bankResponse.getFailureReason());
        return toUpiPaymentAttemptResponse(payment,paymentAttempt,PaymentMessages.failed(bankResponse.getFailureReason()));
    }
    private UpiPaymentAttemptResponse handleUnknown(PaymentAttempt paymentAttempt,Payment payment,BankResponse bankResponse){
        paymentAttempt.markUnknown(bankResponse.getFailureReason());
        return toUpiPaymentAttemptResponse(payment,paymentAttempt,PaymentMessages.unknown());
    }

    private UpiPaymentAttemptResponse toUpiPaymentAttemptResponse(Payment payment,PaymentAttempt paymentAttempt,String paymentMessage){
        return new UpiPaymentAttemptResponse(
                payment.getPaymentId(),
                paymentAttempt.getAttemptId(),
                payment.getStatus(),
                paymentMessage
        );
    }
}
