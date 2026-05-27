package com.nav.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.nav.context.BaseContext;
import com.nav.entity.User;
import com.nav.mapper.UserMapper;
import com.nav.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            response.setStatus(401);
            return false;
        }

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            DecodedJWT decodedJWT = JwtUtil.parseToken(token);
            Long userId = decodedJWT.getClaim("userId").asLong();
            String tokenRole = decodedJWT.getClaim("role").asString();

            User user = userMapper.selectById(userId);
            if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() == 1)) {
                response.setStatus(401);
                return false;
            }

            if (user.getStatus() != null && user.getStatus() != 1) {
                response.setStatus(403);
                return false;
            }

            if (request.getRequestURI().startsWith("/api/v1/admin/")
                    && (!"ADMIN".equalsIgnoreCase(tokenRole) || !"ADMIN".equalsIgnoreCase(user.getRole()))) {
                response.setStatus(403);
                return false;
            }

            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
