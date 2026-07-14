package com.nexupay.payment.common.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecretKeyHasher {

    private final BCryptPasswordEncoder  passwordEncoder = new BCryptPasswordEncoder();

    public String hash(String secretKey){
        return passwordEncoder.encode(secretKey);
    }

    public boolean match(String secretKey,String hash){
        return passwordEncoder.matches(secretKey,hash);
    }
}
