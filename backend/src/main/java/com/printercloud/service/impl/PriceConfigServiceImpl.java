package com.printercloud.service.impl;

import com.printercloud.dto.request.UpdatePriceConfigRequest;
import com.printercloud.dto.response.PriceConfigResponse;
import com.printercloud.entity.PriceConfig;
import com.printercloud.repository.PriceConfigRepository;
import com.printercloud.service.PriceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 价格配置服务实现类
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceConfigServiceImpl implements PriceConfigService {

    private final PriceConfigRepository priceConfigRepository;

    @Override
    public List<PriceConfigResponse> getAllEnabledConfigs() {
        List<PriceConfig> configs = priceConfigRepository.findEnabledConfigs();
        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PriceConfigResponse> getEnabledConfigsByCategory(String category) {
        List<PriceConfig> configs = priceConfigRepository.findEnabledConfigsByCategory(category);
        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PriceConfigResponse> getConfigs(Integer page, Integer size, String category, Integer status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("category", "sortOrder"));
        
        Page<PriceConfig> configPage;
        if (category != null && status != null) {
            configPage = priceConfigRepository.findByCategoryAndStatus(category, status, pageable);
        } else if (status != null) {
            configPage = priceConfigRepository.findByStatus(status, pageable);
        } else {
            configPage = priceConfigRepository.findAll(pageable);
        }
        
        return configPage.map(this::convertToResponse);
    }

    @Override
    public PriceConfigResponse getConfigById(Long id) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findById(id);
        return configOpt.map(this::convertToResponse).orElse(null);
    }

    @Override
    public PriceConfigResponse getConfigByKey(String configKey) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findByConfigKey(configKey);
        return configOpt.map(this::convertToResponse).orElse(null);
    }

    @Override
    @Transactional
    public PriceConfigResponse updateConfig(UpdatePriceConfigRequest request, String updatedBy) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findById(request.getId());
        if (!configOpt.isPresent()) {
            throw new RuntimeException("价格配置不存在");
        }

        PriceConfig config = configOpt.get();
        config.setConfigName(request.getConfigName());
        config.setPriceValue(request.getPriceValue());
        if (request.getUnit() != null) {
            config.setUnit(request.getUnit());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            config.setSortOrder(request.getSortOrder());
        }
        config.setUpdatedBy(updatedBy);

        config = priceConfigRepository.save(config);
        log.info("价格配置更新成功: configKey={}, updatedBy={}", config.getConfigKey(), updatedBy);
        
        return convertToResponse(config);
    }

    @Override
    @Transactional
    public boolean toggleConfigStatus(Long id, String updatedBy) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findById(id);
        if (!configOpt.isPresent()) {
            return false;
        }

        PriceConfig config = configOpt.get();
        config.setStatus(config.getStatus() == 1 ? 0 : 1);
        config.setUpdatedBy(updatedBy);
        priceConfigRepository.save(config);

        log.info("价格配置状态切换成功: configKey={}, status={}, updatedBy={}", 
                config.getConfigKey(), config.getStatus(), updatedBy);
        return true;
    }

    @Override
    public BigDecimal getPriceByKey(String configKey) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findByConfigKey(configKey);
        if (configOpt.isPresent() && configOpt.get().isEnabled()) {
            return configOpt.get().getPriceValue();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculatePrintPrice(String colorType, String paperSize, String duplex, Integer pages, Integer copies) {
        // 构建配置键
        String configKey = String.format("print.%s.%s.%s", 
                colorType.toLowerCase(), 
                duplex.toLowerCase(), 
                paperSize.toLowerCase());
        
        BigDecimal unitPrice = getPriceByKey(configKey);
        if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
            // 如果没有找到具体配置，使用默认价格
            unitPrice = getDefaultPrice(colorType, duplex);
        }
        
        return unitPrice.multiply(BigDecimal.valueOf(pages)).multiply(BigDecimal.valueOf(copies));
    }

    @Override
    @PostConstruct
    @Transactional
    public void initDefaultConfigs() {
        if (priceConfigRepository.count() > 0) {
            log.info("价格配置已存在，跳过初始化");
            return;
        }

        log.info("开始初始化默认价格配置...");
        
        // 黑白打印价格
        createDefaultConfig(PriceConfig.ConfigKey.BW_SINGLE_A4, "A4黑白单面打印", "PRINT", new BigDecimal("0.50"), "元/页", "A4纸张黑白单面打印价格", 1);
        createDefaultConfig(PriceConfig.ConfigKey.BW_DOUBLE_A4, "A4黑白双面打印", "PRINT", new BigDecimal("0.40"), "元/页", "A4纸张黑白双面打印价格", 2);
        createDefaultConfig(PriceConfig.ConfigKey.BW_SINGLE_A3, "A3黑白单面打印", "PRINT", new BigDecimal("1.00"), "元/页", "A3纸张黑白单面打印价格", 3);
        createDefaultConfig(PriceConfig.ConfigKey.BW_DOUBLE_A3, "A3黑白双面打印", "PRINT", new BigDecimal("0.80"), "元/页", "A3纸张黑白双面打印价格", 4);
        
        // 彩色打印价格
        createDefaultConfig(PriceConfig.ConfigKey.COLOR_SINGLE_A4, "A4彩色单面打印", "PRINT", new BigDecimal("1.50"), "元/页", "A4纸张彩色单面打印价格", 5);
        createDefaultConfig(PriceConfig.ConfigKey.COLOR_DOUBLE_A4, "A4彩色双面打印", "PRINT", new BigDecimal("1.20"), "元/页", "A4纸张彩色双面打印价格", 6);
        createDefaultConfig(PriceConfig.ConfigKey.COLOR_SINGLE_A3, "A3彩色单面打印", "PRINT", new BigDecimal("3.00"), "元/页", "A3纸张彩色单面打印价格", 7);
        createDefaultConfig(PriceConfig.ConfigKey.COLOR_DOUBLE_A3, "A3彩色双面打印", "PRINT", new BigDecimal("2.40"), "元/页", "A3纸张彩色双面打印价格", 8);
        
        log.info("默认价格配置初始化完成");
    }

    @Override
    public List<String> getAllCategories() {
        return priceConfigRepository.findAllCategories();
    }

    @Override
    @Transactional
    public boolean batchUpdateConfigs(List<UpdatePriceConfigRequest> requests, String updatedBy) {
        try {
            for (UpdatePriceConfigRequest request : requests) {
                updateConfig(request, updatedBy);
            }
            return true;
        } catch (Exception e) {
            log.error("批量更新价格配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建默认配置
     */
    private void createDefaultConfig(String configKey, String configName, String category, 
                                   BigDecimal priceValue, String unit, String description, Integer sortOrder) {
        PriceConfig config = new PriceConfig();
        config.setConfigKey(configKey);
        config.setConfigName(configName);
        config.setCategory(category);
        config.setPriceValue(priceValue);
        config.setUnit(unit);
        config.setDescription(description);
        config.setSortOrder(sortOrder);
        config.setStatus(1);
        config.setCreatedBy("system");
        config.setUpdatedBy("system");
        
        priceConfigRepository.save(config);
    }

    /**
     * 获取默认价格
     */
    private BigDecimal getDefaultPrice(String colorType, String duplex) {
        if ("COLOR".equalsIgnoreCase(colorType)) {
            return "DOUBLE".equalsIgnoreCase(duplex) ? new BigDecimal("1.20") : new BigDecimal("1.50");
        } else {
            return "DOUBLE".equalsIgnoreCase(duplex) ? new BigDecimal("0.40") : new BigDecimal("0.50");
        }
    }

    /**
     * 转换为响应DTO
     */
    private PriceConfigResponse convertToResponse(PriceConfig config) {
        PriceConfigResponse response = new PriceConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigName(config.getConfigName());
        response.setCategory(config.getCategory());
        response.setCategoryDescription(config.getCategoryDescription());
        response.setPriceValue(config.getPriceValue());
        response.setUnit(config.getUnit());
        response.setFormattedPrice(config.getFormattedPrice());
        response.setDescription(config.getDescription());
        response.setStatus(config.getStatus());
        response.setStatusDescription(config.getStatusDescription());
        response.setSortOrder(config.getSortOrder());
        response.setCreatedTime(config.getCreatedTime());
        response.setUpdatedTime(config.getUpdatedTime());
        response.setCreatedBy(config.getCreatedBy());
        response.setUpdatedBy(config.getUpdatedBy());
        response.setEnabled(config.isEnabled());
        return response;
    }
}
