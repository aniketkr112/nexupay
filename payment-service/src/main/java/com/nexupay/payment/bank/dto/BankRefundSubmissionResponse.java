package com.nexupay.payment.bank.dto;

import com.nexupay.payment.bank.enums.BankRefundSubmissionStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankRefundSubmissionResponse {
    private String refundId;
    private String bankReferenceId;
    private BankRefundSubmissionStatus status;

;}
