package com.printercloud.dto;

/**
 * 登录请求DTO
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
public class LoginRequest {

    private String username;
    private String password;
    private String code; // 微信登录code
    private String loginType; // ADMIN, WECHAT

    public LoginRequest() {
    }

    public LoginRequest(String username, String password, String loginType) {
        this.username = username;
        this.password = password;
        this.loginType = loginType;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
