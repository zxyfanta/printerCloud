package com.printercloud.dto;

import com.printercloud.entity.User;

/**
 * 登录响应DTO
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
public class LoginResponse {

    private String token;
    private User userInfo;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String token, User userInfo) {
        this.token = token;
        this.userInfo = userInfo;
        this.message = "登录成功";
    }

    public LoginResponse(String token, User userInfo, String message) {
        this.token = token;
        this.userInfo = userInfo;
        this.message = message;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
