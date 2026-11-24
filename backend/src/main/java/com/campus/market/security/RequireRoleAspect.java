package com.campus.market.security;

import com.campus.market.common.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 基于 AOP 的角色权限校验切面，支持方法级和类级 @RequireRole
 */
@Aspect
@Component
public class RequireRoleAspect {

    @Pointcut("@annotation(com.campus.market.security.RequireRole) || @within(com.campus.market.security.RequireRole)")
    public void requireRolePointcut() {
    }

    @Before("requireRolePointcut()")
    public void checkRole(JoinPoint joinPoint) {
        RequireRole requireRole = resolveRequireRole(joinPoint);
        if (requireRole == null) {
            return;
        }
        String currentRole = UserContext.getRole();
        if (currentRole == null) {
            throw new BusinessException(401, "未登录");
        }
        String[] allowed = requireRole.value();
        if (allowed.length > 0 && !Arrays.asList(allowed).contains(currentRole)) {
            throw new BusinessException(403, "无权限访问");
        }
    }

    private RequireRole resolveRequireRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole onMethod = method.getAnnotation(RequireRole.class);
        if (onMethod != null) {
            return onMethod;
        }
        return joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
    }
}
