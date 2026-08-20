package com.nexupay.payment.refund.dto.response;

import com.nexupay.payment.refund.enums.RefundStatus;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
public class CreateRefundResponse {
    private String refundId;
    private String paymentId;
    private String merchantRefundId;
    private BigDecimal amount;
    private String currency;
    private RefundStatus status;
}
