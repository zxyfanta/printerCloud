package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.dto.request.CreateOrderRequest;
import com.printercloud.dto.response.OrderResponse;
import com.printercloud.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单", description = "订单管理相关接口")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public R<OrderResponse> create(@RequestBody CreateOrderRequest request, @RequestAttribute("uid") Long uid) {
        return R.success(orderService.createOrder(uid, request));
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public R<Page<OrderResponse>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String startTime,
                                       @RequestParam(required = false) String endTime,
                                       @RequestAttribute("uid") Long uid) {
        int pageIndex = Math.max(0, page - 1); // 小程序从1开始，这里转换为0基
        return R.success(orderService.listOrders(uid, pageIndex, pageSize, status, startTime, endTime));
    }

    @GetMapping("/detail")
    @Operation(summary = "订单详情")
    public R<OrderResponse> detail(@RequestParam("orderId") Long orderId, @RequestAttribute("uid") Long uid) {
        OrderResponse resp = orderService.getOrderDetail(uid, orderId);
        return resp != null ? R.success(resp) : R.notFound("订单不存在");
    }

    @GetMapping("/detail/{orderId}")
    @Operation(summary = "订单详情(REST)")
    public R<OrderResponse> detailRest(@PathVariable Long orderId, @RequestAttribute("uid") Long uid) {
        OrderResponse resp = orderService.getOrderDetail(uid, orderId);
        return resp != null ? R.success(resp) : R.notFound("订单不存在");
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消订单")
    public R<Void> cancel(@RequestParam("orderId") Long orderId, @RequestAttribute("uid") Long uid) {
        return orderService.cancelOrder(uid, orderId) ? R.success("取消成功") : R.error("不可取消");
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "更新订单状态")
    public R<Void> updateStatus(@RequestParam("orderId") Long orderId,
                                @RequestParam("status") String status,
                                @RequestAttribute("uid") Long uid) {
        return orderService.updateOrderStatus(uid, orderId, status) ? R.success("更新成功") : R.error("更新失败");
    }
}
