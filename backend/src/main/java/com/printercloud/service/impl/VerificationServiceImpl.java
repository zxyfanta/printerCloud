package com.printercloud.service.impl;

import com.printercloud.entity.Order;
import com.printercloud.entity.VerificationCode;
import com.printercloud.repository.OrderRepository;
import com.printercloud.repository.VerificationCodeRepository;
import com.printercloud.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final OrderRepository orderRepository;

    @Override
    public String generatePickupCode(Long orderId, Long userId) {
        Order o = orderRepository.findById(orderId).orElse(null);
        if (o == null || !o.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        String code = random6();
        VerificationCode v = new VerificationCode();
        v.setOrderId(orderId);
        v.setCode(code);
        v.setType(VerificationCode.Type.PICKUP.getCode());
        v.setStatus(VerificationCode.Status.ACTIVE.getCode());
        v.setExpireTime(LocalDateTime.now().plusHours(24));
        verificationCodeRepository.save(v);
        return code;
    }

    @Override
    public boolean verifyPickupCode(String code, Long userId) {
        return verificationCodeRepository.findActiveByCode(code, LocalDateTime.now()).map(v -> {
            Order o = orderRepository.findById(v.getOrderId()).orElse(null);
            if (o == null || !o.getUserId().equals(userId)) return false;
            // 标记为已使用
            v.markAsUsed("user:" + userId);
            verificationCodeRepository.save(v);
            // 更新订单状态为完成
            o.setStatus(Order.Status.COMPLETED.getCode());
            o.setPickupTime(LocalDateTime.now());
            orderRepository.save(o);
            return true;
        }).orElse(false);
    }

    private String random6() {
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        return String.valueOf(n);
    }
}
