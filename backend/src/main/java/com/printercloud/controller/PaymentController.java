package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付", description = "支付相关接口(模拟)")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/create")
    @Operation(summary = "创建支付(模拟)")
    public R<Map<String, Object>> create(@RequestParam("orderId") Long orderId, @RequestAttribute("uid") Long uid) {
        return R.success("创建成功", paymentService.createPayment(uid, orderId));
    }

    @PostMapping("/callback")
    @Operation(summary = "支付回调(模拟)")
    public R<Boolean> callback(@RequestParam("orderNo") String orderNo,
                               @RequestParam("status") String status) {
        boolean ok = paymentService.handleCallback(orderNo, status);
        return ok ? R.success(true) : R.error("回调处理失败");
    }

    @GetMapping("/query")
    @Operation(summary = "查询支付状态")
    public R<String> status(@RequestParam("orderId") Long orderId, @RequestAttribute("uid") Long uid) {
        return R.success(paymentService.queryStatus(uid, orderId));
    }
}
