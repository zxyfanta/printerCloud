package com.printercloud.controller;

import com.printercloud.dto.OrderStatisticsDTO;
import com.printercloud.entity.User;
import com.printercloud.service.OrderStatisticsService;
import com.printercloud.service.UserService;
import com.printercloud.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计数据控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private UserService userService;

    /**
     * 获取概览统计数据
     */
    @GetMapping("/overview")
    public R<OrderStatisticsDTO.OverviewStatistics> getOverviewStatistics(@RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            OrderStatisticsDTO.OverviewStatistics overview = orderStatisticsService.getOverviewStatistics();
            return R.ok(overview, "获取成功");
        } catch (Exception e) {
            return R.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取日期范围内的统计数据
     */
    @GetMapping("/daily")
    public R<List<OrderStatisticsDTO.DailyStatistics>> getDailyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            // 限制查询范围，最多90天
            if (startDate.isBefore(endDate.minusDays(90))) {
                startDate = endDate.minusDays(90);
            }

            List<OrderStatisticsDTO.DailyStatistics> dailyStats =
                orderStatisticsService.getDailyStatistics(startDate, endDate);

            return R.ok(dailyStats, "获取成功");
        } catch (Exception e) {
            return R.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart")
    public R<OrderStatisticsDTO.ChartData> getChartData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "total") String type,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            // 限制查询范围，最多90天
            if (startDate.isBefore(endDate.minusDays(90))) {
                startDate = endDate.minusDays(90);
            }

            OrderStatisticsDTO.ChartData chartData =
                orderStatisticsService.getChartData(startDate, endDate, type);

            return R.ok(chartData, "获取成功");
        } catch (Exception e) {
            return R.fail("获取图表数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近N天的统计数据
     */
    @GetMapping("/recent/{days}")
    public R<List<OrderStatisticsDTO.DailyStatistics>> getRecentDaysStatistics(
            @PathVariable int days,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            // 限制天数范围
            if (days > 90) days = 90;
            if (days < 1) days = 7;

            List<OrderStatisticsDTO.DailyStatistics> dailyStats =
                orderStatisticsService.getRecentDaysStatistics(days);

            return R.ok(dailyStats, "获取成功");
        } catch (Exception e) {
            return R.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近N天的图表数据
     */
    @GetMapping("/recent/{days}/chart")
    public R<OrderStatisticsDTO.ChartData> getRecentDaysChartData(
            @PathVariable int days,
            @RequestParam(defaultValue = "total") String type,
            @RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            // 限制天数范围
            if (days > 90) days = 90;
            if (days < 1) days = 7;

            OrderStatisticsDTO.ChartData chartData =
                orderStatisticsService.getRecentDaysChartData(days, type);

            return R.ok(chartData, "获取成功");
        } catch (Exception e) {
            return R.fail("获取图表数据失败: " + e.getMessage());
        }
    }
}
