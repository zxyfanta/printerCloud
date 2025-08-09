package com.printercloud.service;

public interface VerificationService {
    String generatePickupCode(Long orderId, Long userId);
    boolean verifyPickupCode(String code, Long userId);
}
