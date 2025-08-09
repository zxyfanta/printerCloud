package com.printercloud.service;

import java.util.Map;

public interface PaymentService {
    Map<String, Object> createPayment(Long userId, Long orderId);
    boolean handleCallback(String orderNo, String status);
    String queryStatus(Long userId, Long orderId);
}
