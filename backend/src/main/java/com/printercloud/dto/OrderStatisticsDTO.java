package com.printercloud.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单统计数据传输对象
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
public class OrderStatisticsDTO {

    /**
     * 日期统计数据
     */
    public static class DailyStatistics {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        private Long totalOrders;
        private Long pendingOrders;    // 待支付
        private Long paidOrders;       // 已支付
        private Long printingOrders;   // 打印中
        private Long completedOrders;  // 已完成
        private Long cancelledOrders;  // 已取消
        private Long refundedOrders;   // 已退款
        
        private Double totalAmount;
        private Double completedAmount;

        // 构造函数
        public DailyStatistics() {}

        public DailyStatistics(LocalDate date, Long totalOrders, Long pendingOrders, 
                             Long paidOrders, Long printingOrders, Long completedOrders,
                             Long cancelledOrders, Long refundedOrders, 
                             Double totalAmount, Double completedAmount) {
            this.date = date;
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.paidOrders = paidOrders;
            this.printingOrders = printingOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.refundedOrders = refundedOrders;
            this.totalAmount = totalAmount;
            this.completedAmount = completedAmount;
        }

        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Long getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
        }

        public Long getPendingOrders() {
            return pendingOrders;
        }

        public void setPendingOrders(Long pendingOrders) {
            this.pendingOrders = pendingOrders;
        }

        public Long getPaidOrders() {
            return paidOrders;
        }

        public void setPaidOrders(Long paidOrders) {
            this.paidOrders = paidOrders;
        }

        public Long getPrintingOrders() {
            return printingOrders;
        }

        public void setPrintingOrders(Long printingOrders) {
            this.printingOrders = printingOrders;
        }

        public Long getCompletedOrders() {
            return completedOrders;
        }

        public void setCompletedOrders(Long completedOrders) {
            this.completedOrders = completedOrders;
        }

        public Long getCancelledOrders() {
            return cancelledOrders;
        }

        public void setCancelledOrders(Long cancelledOrders) {
            this.cancelledOrders = cancelledOrders;
        }

        public Long getRefundedOrders() {
            return refundedOrders;
        }

        public void setRefundedOrders(Long refundedOrders) {
            this.refundedOrders = refundedOrders;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Double getCompletedAmount() {
            return completedAmount;
        }

        public void setCompletedAmount(Double completedAmount) {
            this.completedAmount = completedAmount;
        }
    }

    /**
     * 图表数据
     */
    public static class ChartData {
        private List<String> dates;
        private Map<String, List<Long>> series;
        private Map<String, String> seriesNames;

        public ChartData() {}

        public ChartData(List<String> dates, Map<String, List<Long>> series, Map<String, String> seriesNames) {
            this.dates = dates;
            this.series = series;
            this.seriesNames = seriesNames;
        }

        public List<String> getDates() {
            return dates;
        }

        public void setDates(List<String> dates) {
            this.dates = dates;
        }

        public Map<String, List<Long>> getSeries() {
            return series;
        }

        public void setSeries(Map<String, List<Long>> series) {
            this.series = series;
        }

        public Map<String, String> getSeriesNames() {
            return seriesNames;
        }

        public void setSeriesNames(Map<String, String> seriesNames) {
            this.seriesNames = seriesNames;
        }
    }

    /**
     * 概览统计
     */
    public static class OverviewStatistics {
        private Long totalOrders;
        private Long todayOrders;
        private Double totalRevenue;
        private Double todayRevenue;
        private Map<Integer, Long> statusCounts;

        public OverviewStatistics() {}

        public OverviewStatistics(Long totalOrders, Long todayOrders, 
                                Double totalRevenue, Double todayRevenue,
                                Map<Integer, Long> statusCounts) {
            this.totalOrders = totalOrders;
            this.todayOrders = todayOrders;
            this.totalRevenue = totalRevenue;
            this.todayRevenue = todayRevenue;
            this.statusCounts = statusCounts;
        }

        public Long getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
        }

        public Long getTodayOrders() {
            return todayOrders;
        }

        public void setTodayOrders(Long todayOrders) {
            this.todayOrders = todayOrders;
        }

        public Double getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(Double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public Double getTodayRevenue() {
            return todayRevenue;
        }

        public void setTodayRevenue(Double todayRevenue) {
            this.todayRevenue = todayRevenue;
        }

        public Map<Integer, Long> getStatusCounts() {
            return statusCounts;
        }

        public void setStatusCounts(Map<Integer, Long> statusCounts) {
            this.statusCounts = statusCounts;
        }
    }
}
