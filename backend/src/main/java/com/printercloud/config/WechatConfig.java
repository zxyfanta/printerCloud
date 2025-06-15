package com.printercloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "wechat.miniprogram")
public class WechatConfig {
    
    private String appid;
    private String secret;
    private String code2sessionUrl;
    
    public String getAppid() {
        return appid;
    }
    
    public void setAppid(String appid) {
        this.appid = appid;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public String getCode2sessionUrl() {
        return code2sessionUrl;
    }
    
    public void setCode2sessionUrl(String code2sessionUrl) {
        this.code2sessionUrl = code2sessionUrl;
    }
}