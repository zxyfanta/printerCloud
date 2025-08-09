package com.printercloud.security;

import com.printercloud.common.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录与公共端点
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/") || uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs") || uri.startsWith("/actuator") || uri.startsWith("/h2-console")) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "未授权");
            return false;
        }
        String token = auth.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute("uid", claims.get("uid"));
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "Token无效或已过期");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        R<Void> r = R.unauthorized(msg);
        new ObjectMapper().writeValue(response.getWriter(), r);
    }
}

