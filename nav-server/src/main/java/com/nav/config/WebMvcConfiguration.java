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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    /**
     * 设置静态资源映射
     * @param registry 资源处理器注册中心
     */
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
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");

        // 1. 映射本地磁盘路径（常用于用户上传的图片、文件）
        // 痛点驱动：前端访问 /download/abc.jpg，Spring 自动去电脑的 D:/nav-uploads/ 目录下找
        registry.addResourceHandler("/download/**")
                .addResourceLocations("file:D:/nav-uploads/");

        // 2. 如果你有前端静态页面、接口文档（如 Knife4j/Swagger）直接放在 resources 里的
        // 顺便一起映射了，防止被 Spring Boot 3 拦截
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("🛡开始加载并部署全局安全拦截防线...");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/api/v1/**") // 保护所有核心接口路径
                .excludePathPatterns(
                        "/api/v1/user/login",     // 排除无需登录的密码登录接口
                        "/api/v1/user/register",  // 排除无需登录的用户注册接口
                        "/doc.html",              // 排除 Knife4j 文档静态页面
                        "/webjars/**",            // 排除文档静态组件资源
                        "/v3/api-docs/**"         // 排除文档接口元数据
                );
    }
}