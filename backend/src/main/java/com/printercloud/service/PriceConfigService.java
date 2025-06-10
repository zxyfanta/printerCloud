package com.printercloud.service;

import com.printercloud.entity.PriceConfig;
import com.printercloud.repository.PriceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 价格配置服务类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class PriceConfigService {

    @Autowired
    private PriceConfigRepository priceConfigRepository;

    /**
     * 获取所有激活的价格配置
     */
    public List<PriceConfig> getAllActivePrices() {
        return priceConfigRepository.findAllActive();
    }

    /**
     * 获取所有价格配置（管理员用）
     */
    public List<PriceConfig> getAllPrices() {
        return priceConfigRepository.findAllNotDeleted();
    }

    /**
     * 根据配置键获取价格
     */
    public BigDecimal getPriceByKey(String configKey) {
        Optional<PriceConfig> config = priceConfigRepository.findByConfigKeyAndDeletedFalse(configKey);
        return config.map(PriceConfig::getPrice).orElse(BigDecimal.ZERO);
    }

    /**
     * 根据ID获取价格配置
     */
    public PriceConfig getPriceConfigById(Long id) {
        return priceConfigRepository.findById(id).orElse(null);
    }

    /**
     * 创建价格配置
     */
    public PriceConfig createPriceConfig(PriceConfig priceConfig) {
        // 检查配置键是否已存在
        if (priceConfigRepository.existsByConfigKeyAndDeletedFalse(priceConfig.getConfigKey())) {
            throw new IllegalArgumentException("配置键已存在: " + priceConfig.getConfigKey());
        }
        
        // 设置排序号
        if (priceConfig.getSortOrder() == null || priceConfig.getSortOrder() == 0) {
            Integer maxOrder = priceConfigRepository.getMaxSortOrder();
            priceConfig.setSortOrder(maxOrder + 1);
        }
        
        return priceConfigRepository.save(priceConfig);
    }

    /**
     * 更新价格配置
     */
    public PriceConfig updatePriceConfig(PriceConfig priceConfig) {
        Optional<PriceConfig> existingOpt = priceConfigRepository.findById(priceConfig.getId());
        if (existingOpt.isPresent()) {
            PriceConfig existing = existingOpt.get();
            
            // 检查配置键是否被其他记录使用
            if (!existing.getConfigKey().equals(priceConfig.getConfigKey())) {
                if (priceConfigRepository.existsByConfigKeyAndDeletedFalse(priceConfig.getConfigKey())) {
                    throw new IllegalArgumentException("配置键已存在: " + priceConfig.getConfigKey());
                }
            }
            
            // 更新字段
            existing.setConfigKey(priceConfig.getConfigKey());
            existing.setConfigName(priceConfig.getConfigName());
            existing.setPrice(priceConfig.getPrice());
            existing.setUnit(priceConfig.getUnit());
            existing.setDescription(priceConfig.getDescription());
            existing.setIsActive(priceConfig.getIsActive());
            existing.setSortOrder(priceConfig.getSortOrder());
            
            return priceConfigRepository.save(existing);
        }
        throw new IllegalArgumentException("价格配置不存在: " + priceConfig.getId());
    }

    /**
     * 删除价格配置（软删除）
     */
    public void deletePriceConfig(Long id) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findById(id);
        if (configOpt.isPresent()) {
            PriceConfig config = configOpt.get();
            config.setDeleted(true);
            priceConfigRepository.save(config);
        } else {
            throw new IllegalArgumentException("价格配置不存在: " + id);
        }
    }

    /**
     * 启用/禁用价格配置
     */
    public void togglePriceConfig(Long id, Boolean isActive) {
        Optional<PriceConfig> configOpt = priceConfigRepository.findById(id);
        if (configOpt.isPresent()) {
            PriceConfig config = configOpt.get();
            config.setIsActive(isActive);
            priceConfigRepository.save(config);
        } else {
            throw new IllegalArgumentException("价格配置不存在: " + id);
        }
    }

    /**
     * 搜索价格配置
     */
    public List<PriceConfig> searchPriceConfigs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPrices();
        }
        return priceConfigRepository.findByConfigNameContainingAndDeletedFalseOrderBySortOrderAsc(keyword.trim());
    }

    /**
     * 计算打印价格
     */
    public BigDecimal calculatePrintPrice(int pages, int copies, boolean isColor, boolean isDoubleSide, String paperSize) {
        // 获取基础价格
        String priceKey = isColor ? "color_print" : "bw_print";
        BigDecimal unitPrice = getPriceByKey(priceKey);
        
        // 计算总价
        BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(pages)).multiply(new BigDecimal(copies));
        
        // 双面打印折扣
        if (isDoubleSide) {
            BigDecimal discount = getPriceByKey("double_side_discount");
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                totalPrice = totalPrice.multiply(discount);
            }
        }
        
        // A3纸张额外费用
        if ("A3".equals(paperSize)) {
            BigDecimal a3Extra = getPriceByKey("a3_extra");
            totalPrice = totalPrice.add(a3Extra.multiply(new BigDecimal(pages)).multiply(new BigDecimal(copies)));
        }
        
        return totalPrice;
    }

    /**
     * 初始化默认价格配置
     */
    public void initDefaultPrices() {
        if (priceConfigRepository.count() > 0) {
            return;
        }

        // 创建默认价格配置
        createPriceConfig(new PriceConfig("bw_print", "黑白打印", new BigDecimal("0.10"), "元/页"));
        createPriceConfig(new PriceConfig("color_print", "彩色打印", new BigDecimal("0.50"), "元/页"));
        createPriceConfig(new PriceConfig("double_side_discount", "双面打印折扣", new BigDecimal("0.80"), "折扣率"));
        createPriceConfig(new PriceConfig("a3_extra", "A3纸张加价", new BigDecimal("0.20"), "元/页"));
        
        System.out.println("默认价格配置初始化完成");
    }
}
