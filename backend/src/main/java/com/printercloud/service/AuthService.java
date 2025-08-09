package com.printercloud.service;

import com.printercloud.dto.response.LoginResponse;

/**
 * 认证服务
 */
public interface AuthService {
    LoginResponse wechatLogin(String code);
    boolean logout(String token);
    LoginResponse refreshToken(String token);
}
