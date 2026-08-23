package com.nexupay.payment.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankRefundRequest {
    private String refundId;
    private String paymentId;
    private BigDecimal amount;
    private String currency;
}
