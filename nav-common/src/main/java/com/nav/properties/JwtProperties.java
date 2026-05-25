package com.nav.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类：将敏感信息与配置解耦，支持多环境动态切换
 */
@Component
@ConfigurationProperties(prefix = "nav.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端生成JWT令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端（移动端）生成JWT令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}