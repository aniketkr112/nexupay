package com.nexupay.payment.bank.service;

import com.nexupay.payment.bank.dto.BankRequest;
import com.nexupay.payment.bank.dto.BankResponse;

public interface BankService {
    BankResponse processPayment(BankRequest request);
    BankResponse checkPaymentStatus(String attemptId);
}
