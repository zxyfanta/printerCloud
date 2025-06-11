package com.printercloud.repository;

import com.printercloud.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键查找配置
     */
    Optional<SystemConfig> findByConfigKeyAndDeletedFalse(String configKey);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKeyAndDeletedFalse(String configKey);

    /**
     * 获取所有未删除的配置
     */
    List<SystemConfig> findByDeletedFalseOrderByCreateTimeDesc();

    /**
     * 根据配置名称搜索
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.deleted = false AND " +
           "(sc.configName LIKE %:keyword% OR sc.configKey LIKE %:keyword% OR sc.description LIKE %:keyword%)")
    List<SystemConfig> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取所有系统配置
     */
    List<SystemConfig> findByIsSystemTrueAndDeletedFalse();

    /**
     * 获取所有用户配置
     */
    List<SystemConfig> findByIsSystemFalseAndDeletedFalse();
}
