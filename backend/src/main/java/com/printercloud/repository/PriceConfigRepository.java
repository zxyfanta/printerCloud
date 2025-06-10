package com.printercloud.repository;

import com.printercloud.entity.PriceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 价格配置数据访问层
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Repository
public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {

    /**
     * 根据配置键查找价格配置
     */
    Optional<PriceConfig> findByConfigKeyAndDeletedFalse(String configKey);

    /**
     * 查找所有激活的价格配置
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.isActive = true AND p.deleted = false ORDER BY p.sortOrder ASC")
    List<PriceConfig> findAllActive();

    /**
     * 查找所有价格配置（包括未激活的）
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.deleted = false ORDER BY p.sortOrder ASC")
    List<PriceConfig> findAllNotDeleted();

    /**
     * 根据配置名称模糊查询
     */
    List<PriceConfig> findByConfigNameContainingAndDeletedFalseOrderBySortOrderAsc(String configName);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKeyAndDeletedFalse(String configKey);

    /**
     * 根据激活状态查找配置
     */
    List<PriceConfig> findByIsActiveAndDeletedFalseOrderBySortOrderAsc(Boolean isActive);

    /**
     * 获取最大排序号
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM PriceConfig p WHERE p.deleted = false")
    Integer getMaxSortOrder();
}
