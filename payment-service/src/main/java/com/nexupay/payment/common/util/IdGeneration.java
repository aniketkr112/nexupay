package com.nexupay.payment.common.util;

import com.github.f4b6a3.ulid.UlidCreator;
import com.nexupay.payment.common.enums.Environment;
import org.springframework.stereotype.Component;

@Component
public class IdGeneration {

    public String generateMerchantId(){
        return "MER_"+ UlidCreator.getUlid().toString();
    }
    public String generateApiKey(Environment environment) {
        return environment.getApiKeyPrefix()+UlidCreator.getUlid().toString();
    }

    public String generateSecretKey(Environment environment) {
        return environment.getSecretKeyPrefix()+UlidCreator.getUlid().toString();
    }

    public String generateCredentialId() {
        return "CRED_"+UlidCreator.getUlid().toString();
    }

    public String generatePaymentId(){
        return "PAY_"+ UlidCreator.getUlid().toString();
    }

    public String generatePaymentAttemptId(){ return "ATT_"+ UlidCreator.getUlid().toString();}

    public String generateBankReferenceId(){ return "BANK_"+ UlidCreator.getUlid().toString();}

    public String generateRefundId(){ return "REFU_"+ UlidCreator.getUlid().toString();}

}
