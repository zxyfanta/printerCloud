package com.printercloud.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "微信登录请求")
public class WechatLoginRequest {

    /**
     * 微信登录凭证code
     */
    @NotBlank(message = "登录凭证不能为空")
    @Schema(description = "微信登录凭证code", example = "081234567890abcdef", required = true)
    private String code;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfoDto userInfo;

    /**
     * 用户信息DTO
     */
    @Data
    @Schema(description = "用户信息")
    public static class UserInfoDto {
        @Schema(description = "昵称", example = "张三")
        private String nickName;

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;

        @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
        private Integer gender;

        @Schema(description = "国家", example = "中国")
        private String country;

        @Schema(description = "省份", example = "广东")
        private String province;

        @Schema(description = "城市", example = "深圳")
        private String city;

        @Schema(description = "语言", example = "zh_CN")
        private String language;
    }
}
