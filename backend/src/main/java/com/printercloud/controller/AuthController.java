package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.dto.request.WechatLoginRequest;
import com.printercloud.dto.response.LoginResponse;
import com.printercloud.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "用户认证相关接口")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public R<LoginResponse> login(@RequestBody WechatLoginRequest request) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            return R.error("登录code不能为空");
        }
        LoginResponse resp = authService.wechatLogin(request.getCode());
        return R.success("登录成功", resp);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public R<LoginResponse> refresh(@RequestHeader(value = "Authorization", required = false) String auth) {
        LoginResponse resp = authService.refreshToken(auth);
        return R.success("刷新成功", resp);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        authService.logout(auth);
        return R.success("登出成功");
    }
}
