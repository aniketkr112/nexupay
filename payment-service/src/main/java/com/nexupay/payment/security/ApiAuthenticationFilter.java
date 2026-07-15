package com.nexupay.payment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexupay.payment.common.constant.HttpHeaders;
import com.nexupay.payment.common.dto.ErrorResponse;
import com.nexupay.payment.common.enums.ErrorCode;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import com.nexupay.payment.credential.service.CredentialAuthenticationService;
import com.nexupay.payment.security.auth.AuthenticatedMerchant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private final ApiCredentialRepository apiCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final CredentialAuthenticationService credentialAuthenticationService;

    public ApiAuthenticationFilter(ApiCredentialRepository apiCredentialRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, CredentialAuthenticationService credentialAuthenticationService) {
        this.apiCredentialRepository = apiCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.credentialAuthenticationService = credentialAuthenticationService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/v1/merchants")
                && request.getMethod().equals(HttpMethod.POST.name());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(HttpHeaders.API_KEY);
        String secretKey = request.getHeader(HttpHeaders.API_SECRET);

        if (apiKey == null || apiKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            sendUnauthorized(response);
            return;
        }
        AuthenticatedMerchant authenticatedMerchant = credentialAuthenticationService.authenticate(apiKey,secretKey);

        if (authenticatedMerchant==null) {
            sendUnauthorized(response);
            return;
        }
        request.setAttribute(SecurityConstants.AUTHENTICATED_MERCHANT,authenticatedMerchant);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_API_CREDENTIALS,"Invalid API credentials");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}