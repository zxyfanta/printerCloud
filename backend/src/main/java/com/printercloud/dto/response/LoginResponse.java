package com.printercloud.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应DTO
 * 
 * @author PrinterCloud Team
 * @since 2024-12-07
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * 登录令牌（JWT）
     */
    @Schema(description = "登录令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;


    /**
     * 过期时间（秒）
     */
    @Schema(description = "过期时间（秒）", example = "86400")
    private Long expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 是否为新用户
     */
    @Schema(description = "是否为新用户", example = "false")
    private Boolean isNewUser;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2024-12-07 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 用户信息DTO
     */
    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID", example = "1")
        private Long id;

        @Schema(description = "用户名", example = "user123")
        private String username;

        @Schema(description = "昵称", example = "张三")
        private String nickname;

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;

        @Schema(description = "性别", example = "1")
        private Integer gender;

        @Schema(description = "性别描述", example = "男")
        private String genderDescription;

        @Schema(description = "手机号", example = "138****8888")
        private String phone;

        @Schema(description = "用户角色", example = "USER")
        private String role;

        @Schema(description = "用户状态", example = "1")
        private Integer status;

        @Schema(description = "状态描述", example = "正常")
        private String statusDescription;

        @Schema(description = "注册时间", example = "2024-12-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTime;
    }
}
