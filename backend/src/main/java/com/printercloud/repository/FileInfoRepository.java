package com.printercloud.repository;

import com.printercloud.entity.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件信息数据访问接口
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * 根据用户ID查找文件
     */
    Page<FileInfo> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找文件
     */
    Page<FileInfo> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

    /**
     * 根据文件ID和用户ID查找文件
     */
    Optional<FileInfo> findByIdAndUserId(Long id, Long userId);

    /**
     * 根据文件哈希查找文件
     */
    Optional<FileInfo> findByFileHash(String fileHash);

    /**
     * 根据文件类型查找文件
     */
    List<FileInfo> findByFileType(String fileType);

    /**
     * 统计用户文件总数
     */
    long countByUserId(Long userId);

    /**
     * 统计用户正常文件数
     */
    long countByUserIdAndStatus(Long userId, Integer status);

    /**
     * 计算用户文件总大小
     */
    @Query("SELECT SUM(f.fileSize) FROM FileInfo f WHERE f.userId = :userId AND f.status = 1")
    Long sumFileSizeByUserId(@Param("userId") Long userId);

    /**
     * 查找用户最近上传的文件
     */
    @Query("SELECT f FROM FileInfo f WHERE f.userId = :userId AND f.status = 1 ORDER BY f.createdTime DESC")
    Page<FileInfo> findRecentFilesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据文件名模糊查询
     */
    @Query("SELECT f FROM FileInfo f WHERE f.userId = :userId AND f.originalName LIKE %:fileName% AND f.status = 1 ORDER BY f.createdTime DESC")
    Page<FileInfo> findByUserIdAndFileNameContaining(@Param("userId") Long userId, @Param("fileName") String fileName, Pageable pageable);

    /**
     * 统计文件类型分布
     */
    @Query("SELECT f.fileType, COUNT(f) as fileCount FROM FileInfo f WHERE f.status = 1 GROUP BY f.fileType ORDER BY fileCount DESC")
    List<Object[]> countByFileType();

    /**
     * 查找大文件
     */
    @Query("SELECT f FROM FileInfo f WHERE f.fileSize > :minSize ORDER BY f.fileSize DESC")
    List<FileInfo> findLargeFiles(@Param("minSize") Long minSize);
}
