package com.printercloud.service;

import com.printercloud.dto.request.CreateOrderRequest;
import com.printercloud.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequest req);
    Page<OrderResponse> listOrders(Long userId, int page, int pageSize, String status, String startTime, String endTime);
    OrderResponse getOrderDetail(Long userId, Long orderId);
    boolean cancelOrder(Long userId, Long orderId);
    boolean updateOrderStatus(Long userId, Long orderId, String status);
}
