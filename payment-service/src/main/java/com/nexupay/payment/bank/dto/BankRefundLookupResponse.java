package com.nexupay.payment.bank.dto;

import com.nexupay.payment.bank.enums.BankRefundLookupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankRefundLookupResponse {
    private String refundId;
    private String bankReferenceId;
    private BankRefundLookupStatus status;
}
