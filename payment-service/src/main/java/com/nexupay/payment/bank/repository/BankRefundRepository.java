package com.nexupay.payment.bank.repository;

import com.nexupay.payment.bank.entity.BankRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRefundRepository
        extends JpaRepository<BankRefund, Long> {

    Optional<BankRefund> findByRefundId(String refundId);
}