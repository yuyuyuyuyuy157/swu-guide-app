package com.nav.utils;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 🎯 核心黑科技：TTS (Text-To-Speech) 语音合成工具类
 */
@Component
@Slf4j
public class TtsUtil {

    // 1. 动态读取路径配置
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    // 2. 动态读取百度 API 密钥
    @Value("${app.baidu.tts.app-id}")
    private String appId;

    @Value("${app.baidu.tts.api-key}")
    private String apiKey;

    @Value("${app.baidu.tts.secret-key}")
    private String secretKey;

    // 百度语音客户端实例
    private AipSpeech client;

    @PostConstruct
    public void init() {
        // 项目启动时，自动初始化百度客户端
        client = new AipSpeech(appId, apiKey, secretKey);
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
    }

    /**
     * 核心方法：将文字合成为 MP3 音频文件并返回本地访问 URL
     */
    public String convertTextToSpeech(String text) {
        log.info("🔊 触发 AI 语音合成，待合成文本长度: {} 字", text.length());

        String fileName = UUID.randomUUID().toString() + ".mp3";
        // 使用 yml 中读取的目录
        File destFile = new File(uploadDir + fileName);

        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            // 🚀 接入真实的百度 API 调用
            TtsResponse res = client.synthesis(text, "zh", 1, null);
            byte[] data = res.getData();

            if (data != null) {
                // 将大厂返回的真实 MP3 二进制流写入物理文件
                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    fos.write(data);
                }
            } else {
                // 如果 data 为 null，说明合成失败（比如密钥填错、欠费等），打印百度返回的错误信息
                log.error("❌ 百度API调用失败: {}", res.getResult());
                throw new RuntimeException("调用百度语音合成接口失败");
            }

            // 使用 yml 中读取的域名拼接 URL
            String finalUrl = baseUrl + fileName;
            log.info("✅ TTS 语音合成完毕！本地物理文件已生成: {}", finalUrl);
            return finalUrl;

        } catch (Exception e) {
            log.error("❌ TTS 语音合成落地失败", e);
            throw new RuntimeException("语音合成失败");
        }
    }
}