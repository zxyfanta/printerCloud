package com.printercloud.repository;

import com.printercloud.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 验证码数据访问接口
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * 根据订单ID查找验证码
     */
    Optional<VerificationCode> findByOrderId(Long orderId);

    /**
     * 根据验证码查找
     */
    Optional<VerificationCode> findByCode(String code);

    /**
     * 根据验证码和类型查找
     */
    Optional<VerificationCode> findByCodeAndType(String code, String type);

    /**
     * 根据订单ID和类型查找验证码
     */
    Optional<VerificationCode> findByOrderIdAndType(Long orderId, String type);

    /**
     * 查找有效的验证码
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.code = :code AND v.status = 'ACTIVE' AND v.expireTime > :currentTime")
    Optional<VerificationCode> findActiveByCode(@Param("code") String code, @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找指定状态的验证码
     */
    List<VerificationCode> findByStatus(String status);

    /**
     * 查找指定类型的验证码
     */
    List<VerificationCode> findByType(String type);

    /**
     * 查找过期的验证码
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.expireTime < :currentTime AND v.status = 'ACTIVE'")
    List<VerificationCode> findExpiredCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计指定状态的验证码数量
     */
    long countByStatus(String status);

    /**
     * 统计指定类型的验证码数量
     */
    long countByType(String type);

    /**
     * 统计今日生成的验证码数量
     */
    @Query("SELECT COUNT(v) FROM VerificationCode v WHERE DATE(v.createdTime) = CURRENT_DATE")
    long countTodayCodes();

    /**
     * 统计今日使用的验证码数量
     */
    @Query("SELECT COUNT(v) FROM VerificationCode v WHERE DATE(v.usedTime) = CURRENT_DATE AND v.status = 'USED'")
    long countTodayUsedCodes();

    /**
     * 批量更新过期验证码状态
     */
    @Modifying
    @Query("UPDATE VerificationCode v SET v.status = 'EXPIRED' WHERE v.expireTime < :currentTime AND v.status = 'ACTIVE'")
    int updateExpiredCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 删除过期的验证码
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expireTime < :currentTime AND v.status IN ('EXPIRED', 'USED')")
    void deleteExpiredCodes(@Param("currentTime") LocalDateTime currentTime);
}
