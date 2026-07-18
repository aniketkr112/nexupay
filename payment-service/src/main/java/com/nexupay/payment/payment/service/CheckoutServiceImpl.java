package com.nexupay.payment.payment.service;

import com.nexupay.payment.common.enums.CheckoutStatus;
import com.nexupay.payment.common.enums.PaymentStatus;
import com.nexupay.payment.common.exception.MerchantNotFoundException;
import com.nexupay.payment.common.exception.PaymentNotFoundException;
import com.nexupay.payment.merchant.entity.Merchant;
import com.nexupay.payment.merchant.repository.MerchantRepository;
import com.nexupay.payment.payment.dto.response.CheckoutResponse;
import com.nexupay.payment.payment.entity.Payment;
import com.nexupay.payment.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CheckoutServiceImpl implements CheckoutService{

    private final PaymentRepository paymentRepository;

    private final MerchantRepository merchantRepository;

    public CheckoutServiceImpl(PaymentRepository paymentRepository, MerchantRepository merchantRepository) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
    }


    @Override
    public CheckoutResponse getCheckout(String paymentId) {
        // TODO(SECURITY):
        // Validate paymentId format before querying the database.

        Payment payment = paymentRepository
                .findByPaymentId(paymentId)
                .orElseThrow(()-> new PaymentNotFoundException(paymentId));

        CheckoutStatus checkoutStatus = resolveCheckoutState(payment);

        Merchant merchant = merchantRepository
                .findById(payment.getMerchantId())
                .orElseThrow(()->new MerchantNotFoundException(payment.getMerchantId()));
        return buildCheckoutResponse(payment,merchant,checkoutStatus);
    }

    private CheckoutStatus resolveCheckoutState(Payment payment){

        if(payment.getStatus()== PaymentStatus.SUCCESS){
            return CheckoutStatus.COMPLETED;
        }
        if(payment.getExpiresAt().isBefore(LocalDateTime.now())){
            return CheckoutStatus.EXPIRED;
        }

        return CheckoutStatus.PAYABLE;
    }

    private CheckoutResponse buildCheckoutResponse(Payment payment,Merchant merchant,CheckoutStatus checkoutStatus){

        CheckoutResponse response = new CheckoutResponse();

        response.setPaymentId(payment.getPaymentId());
        response.setCheckoutStatus(checkoutStatus);
        response.setMerchantName(merchant.getBusinessName());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setCustomerName(payment.getCustomerName());
        response.setExpiresAt(payment.getExpiresAt());
        response.setMessage(getCheckoutMessage(checkoutStatus));

        return response;
    }

    private String getCheckoutMessage(CheckoutStatus status){
        return status.getMessage();
    }
}
