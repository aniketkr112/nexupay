package com.nexupay.payment.security;

import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentMerchantProvider {
    private final HttpServletRequest httpServletRequest;

    public CurrentMerchantProvider(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }


    public AuthenticatedMerchant getCurrentMerchant() {
        return (AuthenticatedMerchant) httpServletRequest.getAttribute(SecurityConstants.AUTHENTICATED_MERCHANT);
    }
}
