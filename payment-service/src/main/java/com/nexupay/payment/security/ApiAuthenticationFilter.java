package com.nexupay.payment.security;

import com.nexupay.payment.credential.entity.ApiCredential;
import com.nexupay.payment.credential.enums.CredentialStatus;
import com.nexupay.payment.credential.repository.ApiCredentialRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private final ApiCredentialRepository apiCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiAuthenticationFilter(ApiCredentialRepository apiCredentialRepository, PasswordEncoder passwordEncoder) {
        this.apiCredentialRepository = apiCredentialRepository;
        this.passwordEncoder = passwordEncoder;
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Optional<ApiCredential> credential = apiCredentialRepository.findByApiKey(apiKey);
        if (credential.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        ApiCredential apiCredential = credential.get();
        boolean validSecret = passwordEncoder.matches(secretKey,apiCredential.getSecretKeyHash());
        if (!validSecret) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (apiCredential.getStatus() != CredentialStatus.ACTIVE) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

}