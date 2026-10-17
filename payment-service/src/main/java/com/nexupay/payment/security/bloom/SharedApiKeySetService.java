package com.nexupay.payment.security.bloom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharedApiKeySetService {

    private static final String REDIS_KEY = "nexupay:api-keys:known";
    private final StringRedisTemplate stringRedisTemplate;

    public void add(String apiKey){

        int maxAttempts = 3;
        for(int attempt=1;attempt<=maxAttempts;attempt++){
            try{
                Long result = stringRedisTemplate.opsForSet().add(REDIS_KEY,apiKey);
                if(result!=null){
                    return;
                }
            } catch (Exception exception) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException(
                            "Failed to register API key in Redis after "
                                    + maxAttempts + " attempts",
                            exception
                    );
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Interrupted while retrying Redis registration",
                        exception
                );
            }
        }
        throw new IllegalStateException(
                "Failed to register API key in Redis"
        );

    }

    public boolean contains(String apikey){
        Boolean result = stringRedisTemplate.opsForSet().isMember(REDIS_KEY,apikey);
        if (result == null) {
            throw new IllegalStateException(
                    "Could not check API key membership in Redis"
            );
        }

        return result;
    }
}
