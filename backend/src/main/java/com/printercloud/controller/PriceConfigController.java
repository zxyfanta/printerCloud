package com.printercloud.controller;

import com.printercloud.entity.PriceConfig;
import com.printercloud.entity.User;
import com.printercloud.service.PriceConfigService;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 价格配置控制器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/price")
@CrossOrigin(origins = "*")
public class PriceConfigController {

    @Autowired
    private PriceConfigService priceConfigService;

    @Autowired
    private UserService userService;

    /**
     * 获取公开价格列表（无需认证）
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicPrices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<PriceConfig> prices = priceConfigService.getAllActivePrices();
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", prices);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取价格信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 计算打印价格
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int pages = (Integer) params.get("pages");
            int copies = (Integer) params.get("copies");
            boolean isColor = (Boolean) params.get("isColor");
            boolean isDoubleSide = (Boolean) params.get("isDoubleSide");
            String paperSize = (String) params.getOrDefault("paperSize", "A4");
            
            BigDecimal totalPrice = priceConfigService.calculatePrintPrice(pages, copies, isColor, isDoubleSide, paperSize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalPrice", totalPrice);
            result.put("pages", pages);
            result.put("copies", copies);
            result.put("isColor", isColor);
            result.put("isDoubleSide", isDoubleSide);
            result.put("paperSize", paperSize);
            
            response.put("code", 200);
            response.put("message", "计算成功");
            response.put("data", result);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "价格计算失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有价格配置（管理员）
     */
    @GetMapping("/admin/list")
    public ResponseEntity<Map<String, Object>> getAllPrices(@RequestHeader("Authorization") String token) {
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
            
            List<PriceConfig> prices = priceConfigService.getAllPrices();
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", prices);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取价格配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建价格配置（管理员）
     */
    @PostMapping("/admin/create")
    public ResponseEntity<Map<String, Object>> createPriceConfig(
            @RequestHeader("Authorization") String token,
            @RequestBody PriceConfig priceConfig) {
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
            
            PriceConfig created = priceConfigService.createPriceConfig(priceConfig);
            response.put("code", 200);
            response.put("message", "创建成功");
            response.put("data", created);
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "创建价格配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新价格配置（管理员）
     */
    @PutMapping("/admin/update")
    public ResponseEntity<Map<String, Object>> updatePriceConfig(
            @RequestHeader("Authorization") String token,
            @RequestBody PriceConfig priceConfig) {
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
            
            PriceConfig updated = priceConfigService.updatePriceConfig(priceConfig);
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", updated);
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新价格配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除价格配置（管理员）
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> deletePriceConfig(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
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
            
            priceConfigService.deletePriceConfig(id);
            response.put("code", 200);
            response.put("message", "删除成功");
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除价格配置失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 启用/禁用价格配置（管理员）
     */
    @PutMapping("/admin/{id}/toggle")
    public ResponseEntity<Map<String, Object>> togglePriceConfig(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> params) {
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
            
            Boolean isActive = params.get("isActive");
            priceConfigService.togglePriceConfig(id, isActive);
            response.put("code", 200);
            response.put("message", isActive ? "启用成功" : "禁用成功");
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "操作失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索价格配置（管理员）
     */
    @GetMapping("/admin/search")
    public ResponseEntity<Map<String, Object>> searchPriceConfigs(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String keyword) {
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
            
            List<PriceConfig> prices = priceConfigService.searchPriceConfigs(keyword);
            response.put("code", 200);
            response.put("message", "搜索成功");
            response.put("data", prices);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "搜索失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
