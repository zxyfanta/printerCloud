package com.printercloud.repository;

import com.printercloud.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据OpenID查找用户
     */
    Optional<User> findByOpenId(String openId);

    /**
     * 根据UnionID查找用户
     */
    Optional<User> findByUnionId(String unionId);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 查找所有管理员用户
     */
    @Query("SELECT u FROM User u WHERE u.role IN ('ADMIN', 'SUPER_ADMIN') AND u.deleted = false")
    List<User> findAllAdmins();

    /**
     * 根据角色查找用户
     */
    List<User> findByRoleAndDeletedFalse(String role);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatusAndDeletedFalse(Integer status);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查OpenID是否存在
     */
    boolean existsByOpenId(String openId);

    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false")
    long countActiveUsers();

    /**
     * 根据角色统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false")
    long countByRole(@Param("role") String role);

    /**
     * 获取所有未删除的用户（分页）
     */
    Page<User> findByDeletedFalse(Pageable pageable);

    /**
     * 根据角色和状态查找用户（分页）
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "u.deleted = false")
    Page<User> findByRoleAndStatus(@Param("role") String role,
                                   @Param("status") Integer status,
                                   Pageable pageable);

    /**
     * 根据搜索条件查找用户（分页）
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.phone LIKE CONCAT('%', :search, '%')) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "u.deleted = false")
    Page<User> findBySearchCriteria(@Param("search") String search,
                                    @Param("role") String role,
                                    @Param("status") Integer status,
                                    Pageable pageable);
}
