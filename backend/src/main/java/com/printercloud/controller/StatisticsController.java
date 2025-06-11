package com.printercloud.controller;

import com.printercloud.dto.OrderStatisticsDTO;
import com.printercloud.entity.User;
import com.printercloud.service.OrderStatisticsService;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> getOverviewStatistics(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            OrderStatisticsDTO.OverviewStatistics overview = orderStatisticsService.getOverviewStatistics();
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", overview);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取统计数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取日期范围内的统计数据
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            // 限制查询范围，最多90天
            if (startDate.isBefore(endDate.minusDays(90))) {
                startDate = endDate.minusDays(90);
            }
            
            List<OrderStatisticsDTO.DailyStatistics> dailyStats = 
                orderStatisticsService.getDailyStatistics(startDate, endDate);
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", dailyStats);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取统计数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart")
    public ResponseEntity<Map<String, Object>> getChartData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "total") String type,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            // 限制查询范围，最多90天
            if (startDate.isBefore(endDate.minusDays(90))) {
                startDate = endDate.minusDays(90);
            }
            
            OrderStatisticsDTO.ChartData chartData = 
                orderStatisticsService.getChartData(startDate, endDate, type);
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", chartData);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取图表数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取最近N天的统计数据
     */
    @GetMapping("/recent/{days}")
    public ResponseEntity<Map<String, Object>> getRecentDaysStatistics(
            @PathVariable int days,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            // 限制天数范围
            if (days > 90) days = 90;
            if (days < 1) days = 7;
            
            List<OrderStatisticsDTO.DailyStatistics> dailyStats = 
                orderStatisticsService.getRecentDaysStatistics(days);
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", dailyStats);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取统计数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取最近N天的图表数据
     */
    @GetMapping("/recent/{days}/chart")
    public ResponseEntity<Map<String, Object>> getRecentDaysChartData(
            @PathVariable int days,
            @RequestParam(defaultValue = "total") String type,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("code", 403);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            // 限制天数范围
            if (days > 90) days = 90;
            if (days < 1) days = 7;
            
            OrderStatisticsDTO.ChartData chartData = 
                orderStatisticsService.getRecentDaysChartData(days, type);
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", chartData);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取图表数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
