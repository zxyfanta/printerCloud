package com.printercloud.repository;

import com.printercloud.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据用户ID查找订单
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找订单
     */
    Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    /**
     * 根据订单ID和用户ID查找订单
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    /**
     * 根据订单号查找订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据订单号和用户ID查找订单
     */
    Optional<Order> findByOrderNoAndUserId(String orderNo, Long userId);

    /**
     * 根据状态查找订单
     */
    List<Order> findByStatus(String status);

    /**
     * 统计用户订单总数
     */
    long countByUserId(Long userId);

    /**
     * 统计用户指定状态的订单数
     */
    long countByUserIdAndStatus(Long userId, String status);

    /**
     * 计算用户指定状态订单的总金额
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.status IN :statuses")
    BigDecimal sumTotalAmountByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<String> statuses);

    /**
     * 查找需要打印的订单（已支付状态）
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PAID' ORDER BY o.createdTime ASC")
    List<Order> findOrdersReadyToPrint();

    /**
     * 查找超时未支付的订单
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING_PAYMENT' AND o.createdTime < :timeoutTime")
    List<Order> findTimeoutUnpaidOrders(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 统计今日订单数
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdTime) = CURRENT_DATE")
    long countTodayOrders();

    /**
     * 统计今日收入
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE DATE(o.createdTime) = CURRENT_DATE AND o.status IN ('PAID', 'PRINTING', 'READY_PICKUP', 'COMPLETED')")
    BigDecimal sumTodayRevenue();

    /**
     * 统计本月订单数
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE YEAR(o.createdTime) = YEAR(CURRENT_DATE) AND MONTH(o.createdTime) = MONTH(CURRENT_DATE)")
    long countThisMonthOrders();

    /**
     * 统计本月收入
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE YEAR(o.createdTime) = YEAR(CURRENT_DATE) AND MONTH(o.createdTime) = MONTH(CURRENT_DATE) AND o.status IN ('PAID', 'PRINTING', 'READY_PICKUP', 'COMPLETED')")
    BigDecimal sumThisMonthRevenue();

    /**
     * 查找用户最近的订单
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdTime DESC")
    Page<Order> findRecentOrdersByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据时间范围查找订单
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdTime BETWEEN :startTime AND :endTime ORDER BY o.createdTime DESC")
    Page<Order> findByUserIdAndCreatedTimeBetween(@Param("userId") Long userId, 
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 Pageable pageable);

    /**
     * 查找热门打印配置统计
     */
    @Query("SELECT o.colorType, o.paperSize, o.duplex, COUNT(o) as orderCount FROM Order o GROUP BY o.colorType, o.paperSize, o.duplex ORDER BY orderCount DESC")
    List<Object[]> findPopularPrintConfigs();
}
