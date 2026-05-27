package com.nav.controller;

import com.nav.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口 - 负责文件上传与基础服务
 * 完全对齐企业级开发规范：接收 MultipartFile，生成 UUID 防重名，返回静态访问 URL
 */
@RestController
@RequestMapping("/api/v1/common")
@Slf4j
@Tag(name = "通用文件上传接口")
public class CommonController {

    // 物理文件存储的绝对路径（与 WebMvcConfiguration 中的映射保持绝对一致）
    private static final String UPLOAD_DIR = "D:/nav-uploads/";

    /**
     * 统一文件上传接口（支持图片、音频等）
     * 接口路径：POST /api/v1/common/upload
     * 权限要求：建议在 JwtTokenInterceptor 中对 /api/v1/common/upload 予以放行（如果允许游客上传头像），或者仅限登录用户
     * @param file 前端表单传来的文件对象 (字段名需对齐前端，通常为 file)
     * @return 文件的网络访问 URL
     */
    @Value("${app.upload.base-url}")
    private String baseUrl;
    @PostMapping("/upload")
    @Operation(summary = "通用文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("📡 接收到文件上传请求，原始文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return Result.error(400, "上传失败：文件内容为空");
        }

        try {
            // 1. 获取文件的原始后缀名 (例如: .jpg, .png, .mp3)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 2. 利用 UUID 构造唯一文件名，防止不同用户上传同名文件被覆盖
            String newFileName = UUID.randomUUID().toString() + extension;

            // 3. 检查并创建物理存储目录
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
                log.info("本地存储目录不存在，已自动创建: {}", UPLOAD_DIR);
            }

            // 4. 将临时文件转存到指定的本地磁盘路径
            File destFile = new File(UPLOAD_DIR + newFileName);
            file.transferTo(destFile);

            // 5. 🎯 拼接网络访问 URL
            // 结合 WebMvcConfiguration 中的 registry.addResourceHandler("/download/**") 映射
            // 这里的域名在生产环境中应替换为真实的 https://api.swu-guide-app.com/
            String fileUrl = baseUrl + newFileName;

            log.info("🔑 文件保存成功！已映射为网络地址: {}", fileUrl);

            return Result.success(fileUrl);

        } catch (IOException e) {
            log.error("❌ 文件上传发生物理写入异常: {}", e.getMessage(), e);
            return Result.error(500, "文件上传失败，服务器本地 I/O 异常");
        }
    }
}