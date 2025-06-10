package com.printercloud.service;

import com.printercloud.entity.User;
import com.printercloud.repository.UserRepository;
import com.printercloud.service.PriceConfigService;
import com.printercloud.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PriceConfigService priceConfigService;

    @Value("${admin.default.username}")
    private String defaultAdminUsername;

    @Value("${admin.default.password}")
    private String defaultAdminPassword;

    @Value("${admin.default.super-username}")
    private String defaultSuperAdminUsername;

    @Value("${admin.default.super-password}")
    private String defaultSuperAdminPassword;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 初始化默认管理员账户
     */
    @PostConstruct
    public void initDefaultAdmins() {
        // 创建默认管理员
        if (!userRepository.existsByUsername(defaultAdminUsername)) {
            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            admin.setStatus(0);
            userRepository.save(admin);
            System.out.println("默认管理员账户已创建: " + defaultAdminUsername);
        }

        // 创建默认超级管理员
        if (!userRepository.existsByUsername(defaultSuperAdminUsername)) {
            User superAdmin = new User();
            superAdmin.setUsername(defaultSuperAdminUsername);
            superAdmin.setPassword(passwordEncoder.encode(defaultSuperAdminPassword));
            superAdmin.setNickname("超级管理员");
            superAdmin.setRole("SUPER_ADMIN");
            superAdmin.setStatus(0);
            userRepository.save(superAdmin);
            System.out.println("默认超级管理员账户已创建: " + defaultSuperAdminUsername);
        }

        // 初始化默认价格配置
        priceConfigService.initDefaultPrices();
    }

    /**
     * 管理员登录
     */
    public String adminLogin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isAdmin() && user.isActive() && passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            }
        }
        return null;
    }

    /**
     * 微信登录（简化版，实际应该调用微信API）
     */
    public User wechatLogin(String code) {
        // 这里应该调用微信API获取用户信息
        // 为了演示，我们创建一个模拟用户
        String openId = "mock_openid_" + code;
        
        Optional<User> userOpt = userRepository.findByOpenId(openId);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            // 创建新用户
            User user = new User();
            user.setOpenId(openId);
            user.setNickname("微信用户");
            user.setRole("USER");
            user.setStatus(0);
            return userRepository.save(user);
        }
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 获取所有管理员
     */
    public List<User> getAllAdmins() {
        return userRepository.findAllAdmins();
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 删除用户（软删除）
     */
    public void deleteUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setDeleted(true);
            userRepository.save(user);
        }
    }

    /**
     * 修改密码
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * 重置密码（管理员功能）
     */
    public boolean resetPassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * 验证token并获取用户
     */
    public User validateTokenAndGetUser(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return getUserById(userId);
            }
        } catch (Exception e) {
            // Token无效
        }
        return null;
    }
}
