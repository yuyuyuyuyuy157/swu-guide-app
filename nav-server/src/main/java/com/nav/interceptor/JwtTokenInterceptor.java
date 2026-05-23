package com.nav.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.nav.context.BaseContext;
import com.nav.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从全局统一请求头中获取登录凭证
        String token = request.getHeader("Authorization");
        // 2. 验证 Token 是否存在
        if (token == null || token.isBlank()) {
            log.warn("拦截：请求头中缺失 Authorization 登录凭证！");
            response.setStatus(401);
            return false;
        }

        try {
            // 处理 Bearer 拼接规范
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 3.调用工具类解析 Token
            DecodedJWT decodedJWT = JwtUtil.parseToken(token);
            Long userId = decodedJWT.getClaim("userId").asLong();

            log.info("JWT 验证通过！当前操作用户ID: {}", userId);

            // 4.将用户ID绑定到当前线程的 ThreadLocal 空间
            BaseContext.setCurrentId(userId);
            return true;
        }catch (Exception ex) {
            log.error("拦截：Token 解析失败或已过期！原因: {}", ex.getMessage());
            response.setStatus(401); // 对应全局错误码 401
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //请求结束后，必须强行清理当前线程的 ThreadLocal 数据，彻底杜绝高并发下的内存泄漏
        BaseContext.removeCurrentId();
    }
}