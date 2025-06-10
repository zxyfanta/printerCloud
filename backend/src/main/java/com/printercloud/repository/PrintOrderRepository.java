package com.printercloud.repository;

import com.printercloud.entity.PrintOrder;
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
 * 打印订单数据访问接口
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Repository
public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long> {

    /**
     * 根据订单号查找订单
     */
    Optional<PrintOrder> findByOrderNo(String orderNo);

    /**
     * 根据验证码查找订单
     */
    Optional<PrintOrder> findByVerifyCode(String verifyCode);

    /**
     * 根据用户ID查找订单
     */
    Page<PrintOrder> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据状态查找订单
     */
    Page<PrintOrder> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    /**
     * 根据用户ID和状态查找订单
     */
    Page<PrintOrder> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Integer status, Pageable pageable);

    /**
     * 查找所有订单，按创建时间倒序
     */
    Page<PrintOrder> findAllByOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 根据时间范围查找订单
     */
    @Query("SELECT o FROM PrintOrder o WHERE o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    List<PrintOrder> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各状态订单数量
     */
    @Query("SELECT o.status, COUNT(o) FROM PrintOrder o GROUP BY o.status")
    List<Object[]> countByStatus();

    /**
     * 统计用户各状态订单数量
     */
    @Query("SELECT o.status, COUNT(o) FROM PrintOrder o WHERE o.userId = :userId GROUP BY o.status")
    List<Object[]> countByUserIdAndStatus(@Param("userId") Long userId);

    /**
     * 查找今日订单
     */
    @Query("SELECT o FROM PrintOrder o WHERE DATE(o.createTime) = CURRENT_DATE ORDER BY o.createTime DESC")
    List<PrintOrder> findTodayOrders();

    /**
     * 查找待处理订单（已支付但未完成）
     */
    @Query("SELECT o FROM PrintOrder o WHERE o.status IN (1, 2) ORDER BY o.payTime ASC")
    List<PrintOrder> findPendingOrders();

    /**
     * 根据验证码模糊查询
     */
    List<PrintOrder> findByVerifyCodeContaining(String verifyCode);
}
