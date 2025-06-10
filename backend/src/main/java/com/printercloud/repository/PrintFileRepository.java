package com.printercloud.repository;

import com.printercloud.entity.PrintFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 打印文件数据访问层
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Repository
public interface PrintFileRepository extends JpaRepository<PrintFile, Long> {

    /**
     * 根据用户ID查找文件
     */
    List<PrintFile> findByUserIdAndDeletedFalseOrderByCreateTimeDesc(Long userId);

    /**
     * 根据用户ID分页查找文件
     */
    Page<PrintFile> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     * 根据文件MD5查找文件
     */
    Optional<PrintFile> findByFileMd5AndDeletedFalse(String fileMd5);

    /**
     * 根据文件名查找文件
     */
    List<PrintFile> findByFileNameContainingAndDeletedFalse(String fileName);

    /**
     * 根据原始文件名查找文件
     */
    List<PrintFile> findByOriginalNameContainingAndDeletedFalse(String originalName);

    /**
     * 根据文件类型查找文件
     */
    List<PrintFile> findByFileTypeAndDeletedFalse(String fileType);

    /**
     * 根据状态查找文件
     */
    List<PrintFile> findByStatusAndDeletedFalse(Integer status);

    /**
     * 查找所有文件（分页）
     */
    Page<PrintFile> findByDeletedFalseOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 根据时间范围查找文件
     */
    @Query("SELECT f FROM PrintFile f WHERE f.createTime BETWEEN :startTime AND :endTime AND f.deleted = false ORDER BY f.createTime DESC")
    List<PrintFile> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户文件数量
     */
    long countByUserIdAndDeletedFalse(Long userId);

    /**
     * 统计文件总数
     */
    @Query("SELECT COUNT(f) FROM PrintFile f WHERE f.deleted = false")
    long countActiveFiles();

    /**
     * 根据文件类型统计数量
     */
    @Query("SELECT COUNT(f) FROM PrintFile f WHERE f.fileType = :fileType AND f.deleted = false")
    long countByFileType(@Param("fileType") String fileType);

    /**
     * 计算用户文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM PrintFile f WHERE f.userId = :userId AND f.deleted = false")
    long sumFileSizeByUserId(@Param("userId") Long userId);
}
