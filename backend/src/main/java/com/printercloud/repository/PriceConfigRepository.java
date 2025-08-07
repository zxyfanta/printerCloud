package com.printercloud.repository;

import com.printercloud.entity.PriceConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 价格配置数据访问接口
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Repository
public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {

    /**
     * 根据配置键查找
     */
    Optional<PriceConfig> findByConfigKey(String configKey);

    /**
     * 根据分类查找
     */
    List<PriceConfig> findByCategory(String category);

    /**
     * 根据分类和状态查找
     */
    List<PriceConfig> findByCategoryAndStatus(String category, Integer status);

    /**
     * 根据分类和状态分页查找
     */
    Page<PriceConfig> findByCategoryAndStatus(String category, Integer status, Pageable pageable);

    /**
     * 根据状态查找
     */
    Page<PriceConfig> findByStatus(Integer status, Pageable pageable);

    /**
     * 查找启用的配置
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.status = 1 ORDER BY p.category, p.sortOrder")
    List<PriceConfig> findEnabledConfigs();

    /**
     * 根据分类查找启用的配置
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.category = :category AND p.status = 1 ORDER BY p.sortOrder")
    List<PriceConfig> findEnabledConfigsByCategory(@Param("category") String category);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 根据配置名称模糊查询
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.configName LIKE %:name% ORDER BY p.category, p.sortOrder")
    Page<PriceConfig> findByConfigNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * 统计各分类的配置数量
     */
    @Query("SELECT p.category, COUNT(p) as configCount FROM PriceConfig p GROUP BY p.category ORDER BY configCount DESC")
    List<Object[]> countByCategory();

    /**
     * 查找所有分类
     */
    @Query("SELECT DISTINCT p.category FROM PriceConfig p ORDER BY p.category")
    List<String> findAllCategories();
}
