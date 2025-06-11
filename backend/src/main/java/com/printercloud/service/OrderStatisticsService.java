package com.printercloud.service;

import com.printercloud.dto.OrderStatisticsDTO;
import com.printercloud.repository.PrintOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单统计服务
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class OrderStatisticsService {

    @Autowired
    private PrintOrderRepository printOrderRepository;

    /**
     * 获取日期范围内的每日统计数据
     */
    public List<OrderStatisticsDTO.DailyStatistics> getDailyStatistics(LocalDate startDate, LocalDate endDate) {
        List<OrderStatisticsDTO.DailyStatistics> result = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.plusDays(1).atStartOfDay();
            
            // 查询当天的订单统计
            Long totalOrders = printOrderRepository.countByCreateTimeBetween(dayStart, dayEnd);
            Long pendingOrders = printOrderRepository.countByStatusAndCreateTimeBetween(0, dayStart, dayEnd);
            Long paidOrders = printOrderRepository.countByStatusAndCreateTimeBetween(1, dayStart, dayEnd);
            Long printingOrders = printOrderRepository.countByStatusAndCreateTimeBetween(2, dayStart, dayEnd);
            Long completedOrders = printOrderRepository.countByStatusAndCreateTimeBetween(3, dayStart, dayEnd);
            Long cancelledOrders = printOrderRepository.countByStatusAndCreateTimeBetween(4, dayStart, dayEnd);
            Long refundedOrders = printOrderRepository.countByStatusAndCreateTimeBetween(5, dayStart, dayEnd);
            
            // 查询当天的金额统计
            Double totalAmount = printOrderRepository.sumAmountByCreateTimeBetween(dayStart, dayEnd);
            Double completedAmount = printOrderRepository.sumAmountByStatusAndCreateTimeBetween(3, dayStart, dayEnd);
            
            if (totalAmount == null) totalAmount = 0.0;
            if (completedAmount == null) completedAmount = 0.0;
            
            OrderStatisticsDTO.DailyStatistics dailyStats = new OrderStatisticsDTO.DailyStatistics(
                currentDate, totalOrders, pendingOrders, paidOrders, printingOrders,
                completedOrders, cancelledOrders, refundedOrders, totalAmount, completedAmount
            );
            
            result.add(dailyStats);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }

    /**
     * 获取图表数据
     */
    public OrderStatisticsDTO.ChartData getChartData(LocalDate startDate, LocalDate endDate, String type) {
        List<OrderStatisticsDTO.DailyStatistics> dailyStats = getDailyStatistics(startDate, endDate);
        
        List<String> dates = dailyStats.stream()
            .map(stat -> stat.getDate().format(DateTimeFormatter.ofPattern("MM-dd")))
            .collect(Collectors.toList());
        
        Map<String, List<Long>> series = new HashMap<>();
        Map<String, String> seriesNames = new HashMap<>();
        
        if ("all".equals(type)) {
            // 显示所有状态
            series.put("pending", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getPendingOrders).collect(Collectors.toList()));
            series.put("paid", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getPaidOrders).collect(Collectors.toList()));
            series.put("printing", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getPrintingOrders).collect(Collectors.toList()));
            series.put("completed", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getCompletedOrders).collect(Collectors.toList()));
            series.put("cancelled", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getCancelledOrders).collect(Collectors.toList()));
            series.put("refunded", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getRefundedOrders).collect(Collectors.toList()));
            
            seriesNames.put("pending", "待支付");
            seriesNames.put("paid", "已支付");
            seriesNames.put("printing", "打印中");
            seriesNames.put("completed", "已完成");
            seriesNames.put("cancelled", "已取消");
            seriesNames.put("refunded", "已退款");
        } else if ("total".equals(type)) {
            // 显示总订单数
            series.put("total", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getTotalOrders).collect(Collectors.toList()));
            seriesNames.put("total", "总订单数");
        } else {
            // 显示特定状态
            switch (type) {
                case "pending":
                    series.put("pending", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getPendingOrders).collect(Collectors.toList()));
                    seriesNames.put("pending", "待支付");
                    break;
                case "paid":
                    series.put("paid", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getPaidOrders).collect(Collectors.toList()));
                    seriesNames.put("paid", "已支付");
                    break;
                case "printing":
                    series.put("printing", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getPrintingOrders).collect(Collectors.toList()));
                    seriesNames.put("printing", "打印中");
                    break;
                case "completed":
                    series.put("completed", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getCompletedOrders).collect(Collectors.toList()));
                    seriesNames.put("completed", "已完成");
                    break;
                case "cancelled":
                    series.put("cancelled", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getCancelledOrders).collect(Collectors.toList()));
                    seriesNames.put("cancelled", "已取消");
                    break;
                case "refunded":
                    series.put("refunded", dailyStats.stream().map(OrderStatisticsDTO.DailyStatistics::getRefundedOrders).collect(Collectors.toList()));
                    seriesNames.put("refunded", "已退款");
                    break;
            }
        }
        
        return new OrderStatisticsDTO.ChartData(dates, series, seriesNames);
    }

    /**
     * 获取概览统计
     */
    public OrderStatisticsDTO.OverviewStatistics getOverviewStatistics() {
        // 总订单数
        Long totalOrders = printOrderRepository.count();
        
        // 今日订单数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        Long todayOrders = printOrderRepository.countByCreateTimeBetween(todayStart, todayEnd);
        
        // 总收入（已完成订单）
        Double totalRevenue = printOrderRepository.sumAmountByStatus(3);
        if (totalRevenue == null) totalRevenue = 0.0;
        
        // 今日收入（已完成订单）
        Double todayRevenue = printOrderRepository.sumAmountByStatusAndCreateTimeBetween(3, todayStart, todayEnd);
        if (todayRevenue == null) todayRevenue = 0.0;
        
        // 各状态订单数量
        Map<Integer, Long> statusCounts = new HashMap<>();
        for (int status = 0; status <= 5; status++) {
            Long count = printOrderRepository.countByStatus(status);
            statusCounts.put(status, count);
        }
        
        return new OrderStatisticsDTO.OverviewStatistics(
            totalOrders, todayOrders, totalRevenue, todayRevenue, statusCounts
        );
    }

    /**
     * 获取最近N天的统计数据
     */
    public List<OrderStatisticsDTO.DailyStatistics> getRecentDaysStatistics(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return getDailyStatistics(startDate, endDate);
    }

    /**
     * 获取最近N天的图表数据
     */
    public OrderStatisticsDTO.ChartData getRecentDaysChartData(int days, String type) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return getChartData(startDate, endDate, type);
    }
}
