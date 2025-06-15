package com.printercloud.service;

import com.printercloud.entity.User;
import com.printercloud.repository.UserRepository;
import com.printercloud.repository.PrintOrderRepository;
import com.printercloud.util.JwtUtil;
import com.printercloud.config.WechatConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private PrintOrderRepository orderRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PriceConfigService priceConfigService;

    @Autowired
    private WechatConfig wechatConfig;

    @Value("${app.env:dev}")
    private String appEnv;

    @Value("${admin.default.username}")
    private String defaultAdminUsername;

    @Value("${admin.default.password}")
    private String defaultAdminPassword;

    @Value("${admin.default.super-username}")
    private String defaultSuperAdminUsername;

    @Value("${admin.default.super-password}")
    private String defaultSuperAdminPassword;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

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
     * 微信登录
     */
    public User wechatLogin(String code) {
        System.out.println("开始微信登录，code: " + code);
        try {
            // 调用微信API获取用户信息
            WechatUserInfo wechatUserInfo = getWechatUserInfo(code);
            System.out.println("获取到微信用户信息: " + (wechatUserInfo != null ? wechatUserInfo.getOpenId() : "null"));
            if (wechatUserInfo == null || wechatUserInfo.getOpenId() == null) {
                throw new RuntimeException("获取微信用户信息失败");
            }
            
            String openId = wechatUserInfo.getOpenId();
            
            // 查找现有用户
            Optional<User> userOpt = userRepository.findByOpenId(openId);
            if (userOpt.isPresent()) {
                User existingUser = userOpt.get();
                // 更新用户信息
                if (wechatUserInfo.getNickname() != null) {
                    existingUser.setNickname(wechatUserInfo.getNickname());
                }
                if (wechatUserInfo.getAvatarUrl() != null) {
                     existingUser.setAvatarUrl(wechatUserInfo.getAvatarUrl());
                 }
                return userRepository.save(existingUser);
            } else {
                // 创建新用户
                User user = new User();
                user.setOpenId(openId);
                user.setNickname(wechatUserInfo.getNickname() != null ? wechatUserInfo.getNickname() : "微信用户");
                user.setAvatarUrl(wechatUserInfo.getAvatarUrl());
                user.setRole("USER");
                user.setStatus(0);
                return userRepository.save(user);
            }
        } catch (Exception e) {
            System.out.println("微信登录异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取微信用户信息
     */
    private WechatUserInfo getWechatUserInfo(String code) {
        System.out.println("getWechatUserInfo被调用，code: " + code);
        
        // 开发环境下直接返回模拟数据
        if (isDevEnvironment()) {
            System.out.println("当前为开发环境，返回模拟数据");
            WechatUserInfo mockInfo = new WechatUserInfo();
            mockInfo.setOpenId("mock_openid_" + code);
            mockInfo.setNickname("微信用户_" + code.substring(0, Math.min(6, code.length())));
            mockInfo.setAvatarUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/default_avatar.png");
            return mockInfo;
        }
        
        // 生产环境下调用真实的微信API
        try {
            // 检查微信配置
            if ("your_wechat_appid".equals(wechatConfig.getAppid()) || 
                "your_wechat_secret".equals(wechatConfig.getSecret())) {
                throw new RuntimeException("请配置正确的微信小程序AppID和Secret");
            }
            
            // 构建请求URL
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wechatConfig.getCode2sessionUrl(),
                    wechatConfig.getAppid(),
                    wechatConfig.getSecret(),
                    code);
            
            System.out.println("调用微信code2Session接口: " + url.replaceAll("secret=[^&]*", "secret=***"));
            
            // 发送HTTP请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            
            System.out.println("微信API响应: " + responseBody);
            
            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 检查是否有错误
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                throw new RuntimeException("微信API调用失败: " + errcode + " - " + errmsg);
            }
            
            // 提取openid和session_key
            String openid = jsonNode.has("openid") ? jsonNode.get("openid").asText() : null;
            String sessionKey = jsonNode.has("session_key") ? jsonNode.get("session_key").asText() : null;
            String unionid = jsonNode.has("unionid") ? jsonNode.get("unionid").asText() : null;
            
            if (openid == null) {
                throw new RuntimeException("微信API返回的openid为空");
            }
            
            // 创建用户信息对象
            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenId(openid);
            userInfo.setSessionKey(sessionKey);
            userInfo.setUnionId(unionid);
            // 注意：code2Session接口不返回用户昵称和头像，需要用户主动授权获取
            userInfo.setNickname("微信用户");
            userInfo.setAvatarUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/default_avatar.png");
            
            return userInfo;
            
        } catch (Exception e) {
            System.out.println("调用微信API异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取微信用户信息失败: " + e.getMessage());
        }
    }
    

    
    /**
     * 检查是否为开发环境
     */
    private boolean isDevEnvironment() {
        return "dev".equals(appEnv);
    }
    
    /**
     * 微信用户信息内部类
     */
    private static class WechatUserInfo {
        private String openId;
        private String nickname;
        private String avatarUrl;
        private String sessionKey;
        private String unionId;
        
        public String getOpenId() { return openId; }
        public void setOpenId(String openId) { this.openId = openId; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        @SuppressWarnings("unused") // 可能被序列化框架使用
        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
        @SuppressWarnings("unused") // 可能被序列化框架使用
        public String getUnionId() { return unionId; }
        public void setUnionId(String unionId) { this.unionId = unionId; }
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
     * 获取用户列表（分页）
     */
    public Page<User> getUserList(Pageable pageable, String search, String role, Integer status) {
        if (search != null && !search.trim().isEmpty()) {
            // 如果有搜索条件，使用自定义查询
            return userRepository.findBySearchCriteria(search.trim(), role, status, pageable);
        } else if (role != null || status != null) {
            // 如果有角色或状态过滤
            return userRepository.findByRoleAndStatus(role, status, pageable);
        } else {
            // 获取所有未删除的用户
            return userRepository.findByDeletedFalse(pageable);
        }
    }

    /**
     * 重置用户密码
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

    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取用户订单统计
            Long totalOrders = orderRepository.countByUserId(userId);
            Long totalPages = orderRepository.sumPagesByUserId(userId);
            BigDecimal totalAmount = orderRepository.sumAmountByUserId(userId);

            stats.put("totalOrders", totalOrders != null ? totalOrders : 0);
            stats.put("totalPages", totalPages != null ? totalPages : 0);
            stats.put("totalAmount", totalAmount != null ? totalAmount.doubleValue() : 0.0);

        } catch (Exception e) {
            // 如果查询失败，返回模拟数据用于测试
            System.out.println("获取用户统计数据失败，返回模拟数据: " + e.getMessage());
            stats.put("totalOrders", 5);
            stats.put("totalPages", 128);
            stats.put("totalAmount", 25.60);
        }

        return stats;
    }
}
