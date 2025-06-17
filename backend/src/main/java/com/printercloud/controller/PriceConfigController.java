package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.entity.PriceConfig;
import com.printercloud.entity.User;
import com.printercloud.service.PriceConfigService;
import com.printercloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public R<List<PriceConfig>> getPublicPrices() {
        try {
            List<PriceConfig> prices = priceConfigService.getAllActivePrices();
            return R.ok(prices, "获取成功");
        } catch (Exception e) {
            return R.fail("获取价格信息失败: " + e.getMessage());
        }
    }

    /**
     * 计算打印价格
     */
    @PostMapping("/calculate")
    public R<Map<String, Object>> calculatePrice(@RequestBody Map<String, Object> params) {
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

            return R.ok(result, "计算成功");
        } catch (Exception e) {
            return R.fail("价格计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有价格配置（管理员）
     */
    @GetMapping("/admin/list")
    public R<List<PriceConfig>> getAllPrices(@RequestHeader("Authorization") String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            List<PriceConfig> prices = priceConfigService.getAllPrices();
            return R.ok(prices, "获取成功");
        } catch (Exception e) {
            return R.fail("获取价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建价格配置（管理员）
     */
    @PostMapping("/admin/create")
    public R<PriceConfig> createPriceConfig(
            @RequestHeader("Authorization") String token,
            @RequestBody PriceConfig priceConfig) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            PriceConfig created = priceConfigService.createPriceConfig(priceConfig);
            return R.ok(created, "创建成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("创建价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新价格配置（管理员）
     */
    @PutMapping("/admin/update")
    public R<PriceConfig> updatePriceConfig(
            @RequestHeader("Authorization") String token,
            @RequestBody PriceConfig priceConfig) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            PriceConfig updated = priceConfigService.updatePriceConfig(priceConfig);
            return R.ok(updated, "更新成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("更新价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除价格配置（管理员）
     */
    @DeleteMapping("/admin/{id}")
    public R<Void> deletePriceConfig(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            priceConfigService.deletePriceConfig(id);
            return R.ok(null, "删除成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("删除价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用价格配置（管理员）
     */
    @PutMapping("/admin/{id}/toggle")
    public R<Void> togglePriceConfig(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> params) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            Boolean isActive = params.get("isActive");
            priceConfigService.togglePriceConfig(id, isActive);
            return R.ok(null, isActive ? "启用成功" : "禁用成功");
        } catch (IllegalArgumentException e) {
            return R.validateFailed(e.getMessage());
        } catch (Exception e) {
            return R.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 搜索价格配置（管理员）
     */
    @GetMapping("/admin/search")
    public R<List<PriceConfig>> searchPriceConfigs(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String keyword) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            User currentUser = userService.validateTokenAndGetUser(token);
            if (currentUser == null || !currentUser.isAdmin()) {
                return R.forbidden("无权限访问");
            }

            List<PriceConfig> prices = priceConfigService.searchPriceConfigs(keyword);
            return R.ok(prices, "搜索成功");
        } catch (Exception e) {
            return R.fail("搜索失败: " + e.getMessage());
        }
    }
}
