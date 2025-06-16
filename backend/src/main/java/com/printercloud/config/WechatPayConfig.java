package com.printercloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayConfig {
    
    private String mchId; // 商户号
    private String mchKey; // 商户密钥
    private String notifyUrl; // 支付结果通知地址
    private Boolean sandboxEnabled = false; // 是否启用沙盒环境
    private String sandboxMchKey; // 沙盒环境商户密钥
    
    // 微信支付API地址
    private String unifiedOrderUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private String sandboxUnifiedOrderUrl = "https://api.mch.weixin.qq.com/sandboxnew/pay/unifiedorder";
    private String orderQueryUrl = "https://api.mch.weixin.qq.com/pay/orderquery";
    private String sandboxOrderQueryUrl = "https://api.mch.weixin.qq.com/sandboxnew/pay/orderquery";
    
    public String getMchId() {
        return mchId;
    }
    
    public void setMchId(String mchId) {
        this.mchId = mchId;
    }
    
    public String getMchKey() {
        return mchKey;
    }
    
    public void setMchKey(String mchKey) {
        this.mchKey = mchKey;
    }
    
    public String getNotifyUrl() {
        return notifyUrl;
    }
    
    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
    
    public Boolean getSandboxEnabled() {
        return sandboxEnabled;
    }
    
    public void setSandboxEnabled(Boolean sandboxEnabled) {
        this.sandboxEnabled = sandboxEnabled;
    }
    
    public String getSandboxMchKey() {
        return sandboxMchKey;
    }
    
    public void setSandboxMchKey(String sandboxMchKey) {
        this.sandboxMchKey = sandboxMchKey;
    }
    
    public String getUnifiedOrderUrl() {
        return unifiedOrderUrl;
    }
    
    public void setUnifiedOrderUrl(String unifiedOrderUrl) {
        this.unifiedOrderUrl = unifiedOrderUrl;
    }
    
    public String getSandboxUnifiedOrderUrl() {
        return sandboxUnifiedOrderUrl;
    }
    
    public void setSandboxUnifiedOrderUrl(String sandboxUnifiedOrderUrl) {
        this.sandboxUnifiedOrderUrl = sandboxUnifiedOrderUrl;
    }
    
    public String getOrderQueryUrl() {
        return orderQueryUrl;
    }
    
    public void setOrderQueryUrl(String orderQueryUrl) {
        this.orderQueryUrl = orderQueryUrl;
    }
    
    public String getSandboxOrderQueryUrl() {
        return sandboxOrderQueryUrl;
    }
    
    public void setSandboxOrderQueryUrl(String sandboxOrderQueryUrl) {
        this.sandboxOrderQueryUrl = sandboxOrderQueryUrl;
    }
    
    /**
     * 获取当前环境下的统一下单URL
     */
    public String getCurrentUnifiedOrderUrl() {
        return sandboxEnabled ? sandboxUnifiedOrderUrl : unifiedOrderUrl;
    }
    
    /**
     * 获取当前环境下的订单查询URL
     */
    public String getCurrentOrderQueryUrl() {
        return sandboxEnabled ? sandboxOrderQueryUrl : orderQueryUrl;
    }
    
    /**
     * 获取当前环境下的商户密钥
     */
    public String getCurrentMchKey() {
        return sandboxEnabled && sandboxMchKey != null ? sandboxMchKey : mchKey;
    }
}