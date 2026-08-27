package com.nexupay.payment.bank.transaction;

import com.nexupay.payment.bank.dto.BankRefundRequest;
import com.nexupay.payment.bank.entity.BankRefund;
import com.nexupay.payment.bank.enums.BankRefundStatus;
import com.nexupay.payment.bank.repository.BankRefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankRefundTransactionService {

    private final BankRefundRepository bankRefundRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createBankRefund(
            BankRefundRequest request,
            String bankReferenceId,
            BankRefundStatus bankStatus) {

        BankRefund bankRefund =
                BankRefund.create(
                        request,
                        bankReferenceId,
                        bankStatus
                );

        bankRefundRepository.save(bankRefund);
    }
}