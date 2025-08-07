package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.dto.request.UpdatePriceConfigRequest;
import com.printercloud.dto.response.PriceConfigResponse;
import com.printercloud.service.PriceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 价格配置控制器
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Slf4j
@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
@Validated
@Tag(name = "价格配置管理", description = "价格配置相关API接口")
public class PriceConfigController {

    private final PriceConfigService priceConfigService;

    /**
     * 获取所有启用的价格配置
     */
    @GetMapping("/configs")
    @Operation(summary = "获取价格配置", description = "获取所有启用的价格配置")
    public R<List<PriceConfigResponse>> getAllConfigs() {
        log.info("获取所有启用的价格配置");
        
        try {
            List<PriceConfigResponse> configs = priceConfigService.getAllEnabledConfigs();
            return R.success("获取价格配置成功", configs);
            
        } catch (Exception e) {
            log.error("获取价格配置失败: {}", e.getMessage(), e);
            return R.error("获取价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取价格配置
     */
    @GetMapping("/configs/category/{category}")
    @Operation(summary = "根据分类获取价格配置", description = "根据分类获取启用的价格配置")
    public R<List<PriceConfigResponse>> getConfigsByCategory(
            @Parameter(description = "配置分类", required = true, example = "PRINT")
            @PathVariable String category) {
        
        log.info("根据分类获取价格配置: category={}", category);
        
        try {
            List<PriceConfigResponse> configs = priceConfigService.getEnabledConfigsByCategory(category);
            return R.success("获取价格配置成功", configs);
            
        } catch (Exception e) {
            log.error("获取价格配置失败: category={}, error={}", category, e.getMessage(), e);
            return R.error("获取价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取价格配置（管理员接口）
     */
    @GetMapping("/admin/configs")
    @Operation(summary = "分页获取价格配置", description = "管理员分页获取价格配置")
    public R<Page<PriceConfigResponse>> getConfigsPage(
            @Parameter(description = "页码，从0开始", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "配置分类", example = "PRINT")
            @RequestParam(required = false) String category,
            @Parameter(description = "状态", example = "1")
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return R.forbidden("无权限执行此操作");
            }
            
            Page<PriceConfigResponse> result = priceConfigService.getConfigs(page, size, category, status);
            return R.success(result);
            
        } catch (Exception e) {
            log.error("分页获取价格配置失败: {}", e.getMessage(), e);
            return R.error("获取价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取价格配置
     */
    @GetMapping("/admin/configs/{id}")
    @Operation(summary = "根据ID获取价格配置", description = "管理员根据ID获取价格配置详情")
    public R<PriceConfigResponse> getConfigById(
            @Parameter(description = "配置ID", required = true)
            @PathVariable @NotNull Long id,
            HttpServletRequest request) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return R.forbidden("无权限执行此操作");
            }
            
            PriceConfigResponse config = priceConfigService.getConfigById(id);
            if (config == null) {
                return R.notFound("价格配置不存在");
            }
            
            return R.success(config);
            
        } catch (Exception e) {
            log.error("获取价格配置失败: id={}, error={}", id, e.getMessage(), e);
            return R.error("获取价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新价格配置
     */
    @PutMapping("/admin/configs")
    @Operation(summary = "更新价格配置", description = "管理员更新价格配置")
    public R<PriceConfigResponse> updateConfig(
            @Valid @RequestBody UpdatePriceConfigRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(httpRequest)) {
                return R.forbidden("无权限执行此操作");
            }
            
            String updatedBy = getCurrentUsername(httpRequest);
            PriceConfigResponse response = priceConfigService.updateConfig(request, updatedBy);
            
            log.info("价格配置更新成功: id={}, updatedBy={}", request.getId(), updatedBy);
            return R.success("价格配置更新成功", response);
            
        } catch (Exception e) {
            log.error("更新价格配置失败: {}", e.getMessage(), e);
            return R.error("更新价格配置失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用价格配置
     */
    @PostMapping("/admin/configs/{id}/toggle")
    @Operation(summary = "启用/禁用价格配置", description = "管理员启用或禁用价格配置")
    public R<Void> toggleConfigStatus(
            @Parameter(description = "配置ID", required = true)
            @PathVariable @NotNull Long id,
            HttpServletRequest request) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return R.forbidden("无权限执行此操作");
            }
            
            String updatedBy = getCurrentUsername(request);
            boolean success = priceConfigService.toggleConfigStatus(id, updatedBy);
            
            if (success) {
                log.info("价格配置状态切换成功: id={}, updatedBy={}", id, updatedBy);
                return R.success("操作成功");
            } else {
                return R.error("价格配置不存在");
            }
            
        } catch (Exception e) {
            log.error("切换价格配置状态失败: id={}, error={}", id, e.getMessage(), e);
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 计算打印价格
     */
    @GetMapping("/calculate")
    @Operation(summary = "计算打印价格", description = "根据打印配置计算价格")
    public R<BigDecimal> calculatePrice(
            @Parameter(description = "颜色类型", required = true, example = "BW")
            @RequestParam String colorType,
            @Parameter(description = "纸张规格", required = true, example = "A4")
            @RequestParam String paperSize,
            @Parameter(description = "单双面", required = true, example = "SINGLE")
            @RequestParam String duplex,
            @Parameter(description = "页数", required = true, example = "3")
            @RequestParam Integer pages,
            @Parameter(description = "份数", required = true, example = "2")
            @RequestParam Integer copies) {
        
        log.info("计算打印价格: colorType={}, paperSize={}, duplex={}, pages={}, copies={}", 
                colorType, paperSize, duplex, pages, copies);
        
        try {
            BigDecimal totalPrice = priceConfigService.calculatePrintPrice(colorType, paperSize, duplex, pages, copies);
            return R.success("价格计算成功", totalPrice);
            
        } catch (Exception e) {
            log.error("计算打印价格失败: {}", e.getMessage(), e);
            return R.error("价格计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有分类", description = "获取所有价格配置分类")
    public R<List<String>> getAllCategories() {
        try {
            List<String> categories = priceConfigService.getAllCategories();
            return R.success("获取分类成功", categories);
            
        } catch (Exception e) {
            log.error("获取分类失败: {}", e.getMessage(), e);
            return R.error("获取分类失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前用户名
     */
    private String getCurrentUsername(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        return username != null ? username : "unknown";
    }

    /**
     * 判断当前用户是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        return "ADMIN".equals(role);
    }
}
