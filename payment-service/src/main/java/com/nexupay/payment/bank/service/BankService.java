package com.nexupay.payment.bank.service;

import com.nexupay.payment.bank.dto.*;

public interface BankService {

    BankResponse processPayment(
            BankRequest request
    );

    BankResponse checkPaymentStatus(
            String attemptId
    );

    BankRefundSubmissionResponse submitRefund(
            BankRefundRequest request
    );

    BankRefundLookupResponse lookupRefund(
            String refundId
    );
}
