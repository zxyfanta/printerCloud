package com.printercloud.service.impl;

import com.printercloud.entity.Order;
import com.printercloud.repository.OrderRepository;
import com.printercloud.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    @Override
    public Map<String, Object> createPayment(Long userId, Long orderId) {
        Order o = orderRepository.findById(orderId).orElse(null);
        if (o == null || !o.getUserId().equals(userId)) throw new IllegalArgumentException("订单不存在");
        Map<String, Object> result = new HashMap<>();
        // 模拟预支付参数
        result.put("nonceStr", "ns" + System.currentTimeMillis());
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("package", "prepay_id=mock" + o.getOrderNo());
        result.put("signType", "MD5");
        result.put("paySign", "mock_sign");
        return result;
    }

    @Override
    public boolean handleCallback(String orderNo, String status) {
        return orderRepository.findByOrderNo(orderNo).map(o -> {
            if ("SUCCESS".equalsIgnoreCase(status)) {
                o.setStatus(Order.Status.PAID.getCode());
                o.setUpdatedTime(LocalDateTime.now());
                orderRepository.save(o);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public String queryStatus(Long userId, Long orderId) {
        Order o = orderRepository.findById(orderId).orElse(null);
        if (o == null || !o.getUserId().equals(userId)) return "NOT_FOUND";
        return o.getStatus();
    }
}
