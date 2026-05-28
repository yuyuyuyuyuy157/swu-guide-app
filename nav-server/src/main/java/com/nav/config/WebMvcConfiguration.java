package com.nav.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.nav.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // 换成这个接口

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer { // 改为实现接口

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("开始生成 Swagger 接口文档...");
        return new OpenAPI()
                .info(new Info()
                        .title("校园导览系统接口文档")
                        .version("1.0")
                        .description("校园导览系统接口定义说明")
                        .contact(new Contact().name("wanyu")));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置自定义静态资源映射...");
        // 只需要映射你电脑本地的特殊磁盘路径，Swagger 自带的那些 Spring Boot 会自动照顾好
        registry.addResourceHandler("/download/**")
                .addResourceLocations("file:D:/nav-uploads/");
    }

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始加载并部署全局安全拦截防线...");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/user/login",
                        "/api/v1/user/register",
                        "/api/v1/scenic/detail/**",
                        "/api/v1/scenic/list",
                        "/api/v1/audio/detail",     // 🎯 核心修正：完美放行用户端获取语音详情的路径
                        "/download/**",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/api/v1/system/config"
                );
    }
}