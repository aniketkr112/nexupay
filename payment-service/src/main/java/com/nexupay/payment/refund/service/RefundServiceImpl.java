package com.nexupay.payment.refund.service;
import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.common.exception.MerchantAccessDeniedException;
import com.nexupay.payment.common.exception.PaymentNotFoundException;
import com.nexupay.payment.common.util.IdGeneration;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import com.nexupay.payment.refund.dto.request.CreateRefundRequest;
import com.nexupay.payment.refund.dto.response.CreateRefundResponse;
import com.nexupay.payment.refund.entity.Refund;
import com.nexupay.payment.refund.enums.RefundStatus;
import com.nexupay.payment.refund.exceptions.LargeAmountRefundException;
import com.nexupay.payment.refund.exceptions.PaymentNotEligibleForRefundException;
import com.nexupay.payment.refund.repository.RefundRepository;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService{
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final IdGeneration idGeneration;


    @Transactional
    @Override
    public CreateRefundResponse createRefund(AuthenticatedMerchant merchant, CreateRefundRequest request) {

        String paymentId = request.getPaymentId();
        String merchantRefundId = request.getMerchantRefundId();

        Payment payment = paymentRepository
                .findByPaymentIdForUpdate(paymentId)
                .orElseThrow(()->new PaymentNotFoundException(paymentId));

        if(!Objects.equals(payment.getMerchantId(), merchant.getId())){
            throw new MerchantAccessDeniedException("Payment not found by merchant");
        }

        if(payment.getStatus()!= PaymentStatus.SUCCESS){
            throw new PaymentNotEligibleForRefundException("Payment is not eligible for refund");
        }
        Optional<Refund> existingRefund = findExistingRefund(merchantRefundId,paymentId);

        if(existingRefund.isPresent()){
            log.info(
                    "Idempotent retry detected.  MerchantRefundId={}, PaymentId={}",
                    merchantRefundId,
                    paymentId
            );
            return toRefundResponse(existingRefund.get());
        }

        BigDecimal committedRefundAmount = refundRepository
                .sumRefundAmountByPaymentIdAndStatusIn(
                        paymentId,
                        List.of(RefundStatus.PENDING,RefundStatus.SUCCESS)
                );

        BigDecimal remainingRefundableAmount = payment.getAmount().subtract(committedRefundAmount);
        if(request.getAmount().compareTo(remainingRefundableAmount)>0){
            throw new LargeAmountRefundException("Refund amount exceeds the remaining refundable amount");
        }

        String refundId = idGeneration.generateRefundId();
        Refund refund = Refund.create(
                refundId,
                merchantRefundId,
                payment,
                request
        );

        try {
            Refund savedRefund = refundRepository.save(refund);
            return toRefundResponse(savedRefund);
        } catch (DataIntegrityViolationException ex) {

            log.info(
                    "Concurrent refund creation detected. MerchantRefundId={}, PaymentId={}",
                    merchantRefundId,
                    paymentId
            );

            Refund alreadyExistRefund =
                    findExistingRefund(merchantRefundId,paymentId)
                            .orElseThrow(()->ex);
            return toRefundResponse(alreadyExistRefund);
        }

    }

    private Optional<Refund> findExistingRefund(String merchantRefundId,String paymentId){
        return refundRepository.findByPaymentIdAndMerchantRefundId(paymentId,merchantRefundId);
    }

    private CreateRefundResponse toRefundResponse(Refund refund){
        CreateRefundResponse response = new CreateRefundResponse();
        response.setRefundId(refund.getRefundId());
        response.setPaymentId(refund.getPaymentId());
        response.setMerchantRefundId(refund.getMerchantRefundId());
        response.setAmount(refund.getAmount());
        response.setCurrency(refund.getCurrency());
        response.setStatus(refund.getStatus());

        return response;
    }
}

