package com.nexupay.payment.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class BankRequest {
    private String transactionId;
    private BigDecimal amount;
    private String upiId;


}
