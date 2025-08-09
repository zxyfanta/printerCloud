package com.printercloud.service.impl;

import com.printercloud.dto.request.CreateOrderRequest;
import com.printercloud.dto.response.OrderResponse;
import com.printercloud.entity.FileInfo;
import com.printercloud.entity.Order;
import com.printercloud.repository.FileInfoRepository;
import com.printercloud.repository.OrderRepository;
import com.printercloud.service.OrderService;
import com.printercloud.service.PriceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final FileInfoRepository fileInfoRepository;
    private final PriceConfigService priceConfigService;

    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest req) {
        Long fileId = req.getFileInfo() != null ? req.getFileInfo().getServerId() : null;
        FileInfo fi = (fileId != null) ? fileInfoRepository.findByIdAndUserId(fileId, userId).orElse(null) : null;
        if (fi == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        int pages = req.getEstimatedPages() != null ? req.getEstimatedPages() : Math.max(1, fi.getPageCount());
        int copies = req.getPrintConfig().getCopies();
        String colorType = req.getPrintConfig().getColorType();
        String paperSize = req.getPrintConfig().getPaperSize();
        String duplex = req.getPrintConfig().getDuplex();
        
        BigDecimal total = priceConfigService.calculatePrintPrice(colorType, paperSize, duplex, pages, copies);
        BigDecimal unit = total.divide(BigDecimal.valueOf(pages)).divide(BigDecimal.valueOf(copies));

        Order o = new Order();
        o.setOrderNo(genOrderNo());
        o.setUserId(userId);
        o.setFileId(fi.getId());
        o.setCopies(copies);
        o.setColorType(colorType);
        o.setPaperSize(paperSize);
        o.setDuplex(duplex);
        o.setEstimatedPages(pages);
        o.setUnitPrice(unit);
        o.setTotalAmount(total);
        o.setStatus(Order.Status.PENDING_PAYMENT.getCode());
        o.setCreatedTime(LocalDateTime.now());
        o.setUpdatedTime(LocalDateTime.now());
        orderRepository.save(o);

        return toResponse(o, fi);
    }

    @Override
    public Page<OrderResponse> listOrders(Long userId, int page, int pageSize, String status, String startTime, String endTime) {
        Page<Order> p;
        if (status != null && !status.isEmpty()) {
            p = orderRepository.findByUserIdAndStatus(userId, status, PageRequest.of(page, pageSize));
        } else {
            p = orderRepository.findByUserId(userId, PageRequest.of(page, pageSize));
        }
        return p.map(o -> toResponse(o, null));
    }

    @Override
    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order o = orderRepository.findById(orderId).orElse(null);
        if (o == null || !o.getUserId().equals(userId)) return null;
        FileInfo fi = fileInfoRepository.findById(o.getFileId()).orElse(null);
        return toResponse(o, fi);
    }

    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        return orderRepository.findById(orderId).map(o -> {
            if (!o.getUserId().equals(userId)) return false;
            if (!o.canCancel()) return false;
            o.setStatus(Order.Status.CANCELLED.getCode());
            o.setUpdatedTime(LocalDateTime.now());
            orderRepository.save(o);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean updateOrderStatus(Long userId, Long orderId, String status) {
        return orderRepository.findById(orderId).map(o -> {
            if (!o.getUserId().equals(userId)) return false;
            o.setStatus(status);
            o.setUpdatedTime(LocalDateTime.now());
            orderRepository.save(o);
            return true;
        }).orElse(false);
    }

    private String genOrderNo() {
        return "PRT" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
    }

    private OrderResponse toResponse(Order o, FileInfo fi) {
        OrderResponse resp = new OrderResponse();
        resp.setId(o.getId());
        resp.setOrderNo(o.getOrderNo());

        OrderResponse.PrintConfigResponse pc = new OrderResponse.PrintConfigResponse();
        pc.setCopies(o.getCopies());
        pc.setColorType(o.getColorType());
        pc.setColorTypeDescription("COLOR".equalsIgnoreCase(o.getColorType()) ? "彩色" : "黑白");
        pc.setPaperSize(o.getPaperSize());
        pc.setPaperSizeDescription(o.getPaperSize());
        pc.setDuplex(o.getDuplex());
        pc.setDuplexDescription("DOUBLE".equalsIgnoreCase(o.getDuplex()) ? "双面" : "单面");
        pc.setEstimatedPages(o.getEstimatedPages());
        pc.setUnitPrice(o.getUnitPrice());
        resp.setPrintConfig(pc);

        if (fi == null) {
            fi = fileInfoRepository.findById(o.getFileId()).orElse(null);
        }
        if (fi != null) {
            OrderResponse.FileInfoResponse fr = new OrderResponse.FileInfoResponse();
            fr.setId(fi.getId());
            fr.setOriginalName(fi.getOriginalName());
            fr.setFileSize(fi.getFileSize());
            fr.setFormattedFileSize(fi.getFormattedFileSize());
            fr.setFileType(fi.getFileType());
            fr.setFileTypeIcon(fi.getFileTypeIcon());
            fr.setPageCount(fi.getPageCount());
            resp.setFileInfo(fr);
        }

        resp.setTotalAmount(o.getTotalAmount());
        resp.setStatus(o.getStatus());
        resp.setStatusDescription(o.getStatusDescription());
        resp.setCreatedTime(o.getCreatedTime());
        resp.setUpdatedTime(o.getUpdatedTime());

        OrderResponse.OperationPermission ops = new OrderResponse.OperationPermission();
        ops.setCanCancel(o.canCancel());
        ops.setCanPay(o.canPay());
        ops.setCanPickup(o.canPickup());
        resp.setOperations(ops);

        return resp;
    }
}
