package com.printercloud.service;

import com.printercloud.dto.request.UpdatePriceConfigRequest;
import com.printercloud.dto.response.PriceConfigResponse;
import com.printercloud.entity.PriceConfig;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格配置服务接口
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
public interface PriceConfigService {

    /**
     * 获取所有启用的价格配置
     */
    List<PriceConfigResponse> getAllEnabledConfigs();

    /**
     * 根据分类获取启用的价格配置
     */
    List<PriceConfigResponse> getEnabledConfigsByCategory(String category);

    /**
     * 分页获取价格配置
     */
    Page<PriceConfigResponse> getConfigs(Integer page, Integer size, String category, Integer status);

    /**
     * 根据ID获取价格配置
     */
    PriceConfigResponse getConfigById(Long id);

    /**
     * 根据配置键获取价格配置
     */
    PriceConfigResponse getConfigByKey(String configKey);

    /**
     * 更新价格配置
     */
    PriceConfigResponse updateConfig(UpdatePriceConfigRequest request, String updatedBy);

    /**
     * 启用/禁用价格配置
     */
    boolean toggleConfigStatus(Long id, String updatedBy);

    /**
     * 根据配置键获取价格值
     */
    BigDecimal getPriceByKey(String configKey);

    /**
     * 计算打印价格
     */
    BigDecimal calculatePrintPrice(String colorType, String paperSize, String duplex, Integer pages, Integer copies);

    /**
     * 初始化默认价格配置
     */
    void initDefaultConfigs();

    /**
     * 获取所有分类
     */
    List<String> getAllCategories();

    /**
     * 批量更新价格配置
     */
    boolean batchUpdateConfigs(List<UpdatePriceConfigRequest> requests, String updatedBy);
}
