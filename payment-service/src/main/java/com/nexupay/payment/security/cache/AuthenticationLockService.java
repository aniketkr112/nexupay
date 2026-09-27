package com.nexupay.payment.security.cache;

import java.time.Duration;

public interface AuthenticationLockService {

    boolean acquireLock(String lockKey, String lockValue, Duration ttl);

    void releaseLock(String lockKey,String lockValue);
}
