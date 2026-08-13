package com.nexupay.payment.payment.repository;

import com.nexupay.payment.common.enums.PaymentWebhookStatus;
import com.nexupay.payment.payment.entity.PaymentWebhook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook,Long> {

    List<PaymentWebhook> findByStatus(
            PaymentWebhookStatus status,
            Pageable pageable
    );
}
