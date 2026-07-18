package com.nexupay.payment.payment.repository;

import com.nexupay.payment.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByMerchantIdAndMerchantOrderId(
            Long merchantId,
            String merchantOrderId
    );

    Optional<Payment> findByPaymentId(String paymentId);
}
