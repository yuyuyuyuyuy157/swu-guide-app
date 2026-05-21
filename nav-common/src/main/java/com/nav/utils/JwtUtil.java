package com.nav.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.Map;
import com.auth0.jwt.JWTCreator;

public class JwtUtil {
    private static final String SECRET_KEY = "SWU_Nav_Guide_Secret_Key_Yubu";
    // 生成 JWT Token 有效期7天
    public static String createToken(Map<String, Object> claims) {
        long expirationTime = 1000L * 60 * 60 * 24 * 7;
        Date expDate = new Date(System.currentTimeMillis() + expirationTime);
        JWTCreator.Builder builder = JWT.create();
        // 将 claims 中的所有键值对平铺写入 JWT 的 Payload 中
        if (claims != null) {
            claims.forEach((key, value) -> {
                if (value instanceof String) {
                    builder.withClaim(key, (String) value);
                } else if (value instanceof Long) {
                    builder.withClaim(key, (Long) value);
                } else if (value instanceof Integer) {
                    builder.withClaim(key, (Integer) value);
                } else {
                    // 或者统一转成 String 存储，解析时再转回来
                    builder.withClaim(key, String.valueOf(value));
                }
            });
        }

        return builder
                .withExpiresAt(expDate) // 过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用 HMAC256 加密算法签名
    }
    // 解析并验证 JWT Token
    public static DecodedJWT parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token); // 如果过期、伪造，自动抛出 JWTVerificationException 异常
    }
}