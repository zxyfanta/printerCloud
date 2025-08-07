package com.printercloud.repository;

import com.printercloud.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

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
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 查找正常状态的用户
     */
    @Query("SELECT u FROM User u WHERE u.status = 1 AND u.deleted = false")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * 根据角色查找用户
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * 根据状态查找用户
     */
    Page<User> findByStatus(Integer status, Pageable pageable);

    /**
     * 统计正常用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1 AND u.deleted = false")
    long countActiveUsers();

    /**
     * 统计管理员数量
     */
    long countByRole(String role);

    /**
     * 检查OpenID是否存在
     */
    boolean existsByOpenId(String openId);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据昵称模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% AND u.deleted = false")
    Page<User> findByNicknameLike(@Param("nickname") String nickname, Pageable pageable);

    /**
     * 查找最近注册的用户
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false ORDER BY u.createdTime DESC")
    Page<User> findRecentUsers(Pageable pageable);
}
