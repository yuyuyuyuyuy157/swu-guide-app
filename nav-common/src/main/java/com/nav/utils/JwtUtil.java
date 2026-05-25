package com.nav.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.Map;
import com.auth0.jwt.JWTCreator;

/**
 * JWT 核心令牌工具类
*/
public class JwtUtil {

    // 默认兜底静态密钥
    private static final String DEFAULT_SECRET_KEY = "SWU_Nav_Guide_Secret_Key_Yubu";

    /**
     * 1️生成标准 7 天有效期的 Token（供用户登录端免改动调用）
     */
    public static String createToken(Map<String, Object> claims) {
        long defaultExpirationTime = 1000L * 60 * 60 * 24 * 7; // 7天（毫秒）
        return createToken(claims, defaultExpirationTime, DEFAULT_SECRET_KEY);
    }

    /**
     * 2️支持动态时效与动态密钥生成 Token（供管理员后台、多客户端调用）
     */
    public static String createToken(Map<String, Object> claims, long expirationTime, String secretKey) {
        // 计算绝对过期时间点
        Date expDate = new Date(System.currentTimeMillis() + expirationTime);
        JWTCreator.Builder builder = JWT.create();

        // 将 claims 中的所有键值对平铺写入 JWT 的 Payload 中
        if (claims != null) {
            claims.forEach((key, value) -> {
                if (value instanceof String stringVal) {
                    builder.withClaim(key, stringVal);
                } else if (value instanceof Long longVal) {
                    builder.withClaim(key, longVal);
                } else if (value instanceof Integer intVal) {
                    builder.withClaim(key, intVal);
                } else {
                    // 无法识别的通用对象统一转成 String 存储
                    builder.withClaim(key, String.valueOf(value));
                }
            });
        }

        return builder
                .withExpiresAt(expDate) // 设置动态过期时间
                .sign(Algorithm.HMAC256(secretKey)); // 使用动态配置的密钥进行签名
    }

    /**
     * 3使用默认静态密钥解析并验证 Token
     */
    public static DecodedJWT parseToken(String token) {
        return parseToken(token, DEFAULT_SECRET_KEY);
    }

    /**
     * 4使用动态指定的配置密钥解析并验证 Token
     */
    public static DecodedJWT parseToken(String token, String secretKey) {
        return JWT.require(Algorithm.HMAC256(secretKey))
                .build()
                .verify(token); // 如果过期、篡改、伪造，会自动抛出 JWTVerificationException
    }
}