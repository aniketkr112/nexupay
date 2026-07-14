package com.nexupay.payment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexupay.payment.common.dto.ErrorResponse;
import com.nexupay.payment.common.enums.ErrorCode;
import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.credential.enums.CredentialStatus;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
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
import java.util.Optional;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private final ApiCredentialRepository apiCredentialRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper;

    public ApiAuthenticationFilter(ApiCredentialRepository apiCredentialRepository, ObjectMapper objectMapper) {
        this.apiCredentialRepository = apiCredentialRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/v1/merchants")
                && request.getMethod().equals(HttpMethod.POST.name());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        String secretKey = request.getHeader("X-API-Secret");

        if (apiKey == null || apiKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            sendUnauthorized(response);
            return;
        }

        Optional<ApiCredential> credential = apiCredentialRepository.findByApiKey(apiKey);
        if (credential.isEmpty()) {
            sendUnauthorized(response);
            return;
        }
        ApiCredential apiCredential = credential.get();
        boolean validSecret = passwordEncoder.matches(secretKey,apiCredential.getSecretKeyHash());
        if (!validSecret) {
            sendUnauthorized(response);
            return;
        }
        if (apiCredential.getStatus() != CredentialStatus.ACTIVE) {

            sendUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_API_CREDENTIALS,"Invalid API credentials");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}