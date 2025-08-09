package com.printercloud.service.impl;

import com.printercloud.dto.response.LoginResponse;
import com.printercloud.entity.User;
import com.printercloud.repository.UserRepository;
import com.printercloud.security.JwtUtil;
import com.printercloud.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final com.printercloud.security.JwtUtil jwtUtil;

    @Override
    public LoginResponse wechatLogin(String code) {
        // 模拟通过code从微信交换openId
        String mockOpenId = "mock_" + code.substring(Math.max(0, code.length() - 8));

        // 查找或创建用户
        User user = userRepository.findByOpenId(mockOpenId).orElseGet(() -> {
            User u = new User();
            u.setOpenId(mockOpenId);
            u.setNickname("微信用户" + mockOpenId.substring(Math.max(0, mockOpenId.length() - 4)));
            u.setAvatarUrl("");
            return userRepository.save(u);
        });

        // 生成模拟token
        String token = jwtUtil.generateToken(user.getId());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setExpiresIn(24 * 3600L);
        resp.setIsNewUser(false);
        resp.setLoginTime(LocalDateTime.now());

        LoginResponse.UserInfo ui = new LoginResponse.UserInfo();
        ui.setId(user.getId());
        ui.setUsername(user.getUsername());
        ui.setNickname(user.getNickname());
        ui.setAvatarUrl(user.getAvatarUrl());
        ui.setGender(user.getGender());
        ui.setGenderDescription(user.getGenderDescription());
        ui.setPhone(user.getPhone());
        ui.setRole(user.getRole());
        ui.setStatus(user.getStatus());
        ui.setStatusDescription(user.getStatusDescription());
        ui.setCreatedTime(user.getCreatedTime());
        resp.setUserInfo(ui);

        return resp;
    }

    @Override
    public boolean logout(String token) {
        // 模拟登出（无状态）
        log.info("logout token={}", token);
        return true;
    }

    @Override
    public LoginResponse refreshToken(String token) {
        // 模拟刷新
        LoginResponse resp = new LoginResponse();
        resp.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
        resp.setExpiresIn(24 * 3600L);
        resp.setLoginTime(LocalDateTime.now());
        return resp;
    }
}
