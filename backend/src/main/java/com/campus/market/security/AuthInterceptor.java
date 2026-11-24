package com.campus.market.security;

import com.campus.market.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：校验 JWT 并写入 UserContext
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 放行公开的商品浏览/评价查看接口
        if (isPublicBrowsing(request)) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录");
        }
        String token = auth.substring(7);
        try {
            Long userId = jwtUtil.parseUserId(token);
            String role = jwtUtil.parseRole(token);
            UserContext.set(userId, role);
            return true;
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 公开的浏览接口：商品搜索、商品详情、评价查看
     */
    private boolean isPublicBrowsing(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri.equals("/api/products")
                || uri.matches("/api/products/\\d+")
                || uri.matches("/api/products/\\d+/reviews")
                || uri.matches("/api/merchants/\\d+/reviews");
    }
}
