package com.campus.market.security;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（基于 Hutool）
 */
@Component
public class JwtUtil {

    @Value("${campus.jwt.secret}")
    private String secret;

    @Value("${campus.jwt.expire-days}")
    private int expireDays;

    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireDays * 24L * 3600 * 1000);
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("role", role)
                .setIssuedAt(now)
                .setExpiresAt(expire)
                .setKey(secret.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    /**
     * 校验并解析 token，返回 userId；失败抛异常
     */
    public Long parseUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        jwt.setKey(secret.getBytes(StandardCharsets.UTF_8));
        if (!jwt.verify()) {
            throw new RuntimeException("token 校验失败");
        }
        return Long.parseLong(jwt.getPayload("userId").toString());
    }

    public String parseRole(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object role = jwt.getPayload("role");
        return role == null ? null : role.toString();
    }
}
