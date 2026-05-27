package com.nav.controller;

import com.nav.result.Result;
import com.nav.service.AudioService;
import com.nav.vo.AudioDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 🎯 用户端 - 音频播放与语音讲解控制器
 * 严格对齐《接口文档5.18.1》3.1 节契约
 */
@RestController
@RequestMapping("/api/v1/audio")
@Slf4j
@Tag(name = "用户端-音频播放模块接口")
public class AudioController {

    @Autowired
    private AudioService audioService;

    /**
     * 3.1 获取景点语音详情
     * 接口路径：GET /api/v1/audio/detail
     * 痛点驱动：支持前端自动和手动触发播放器组件，配合 AI TTS 懒加载技术实现首播转化
     * @param audioId 语音（景点）唯一ID
     * @return 统一返回封装的音频详情
     */
    @GetMapping("/detail")
    @Operation(summary = "获取景点语音详情")
    public Result<AudioDetailVO> getAudioDetail(@RequestParam String audioId) {
        log.info("📡 收到获取景点语音详情请求，目标语音ID(景点ID): {}", audioId);

        Long id;
        try {
            id = Long.valueOf(audioId);
        } catch (NumberFormatException e) {
            return Result.error(400, "非法的语音ID格式");
        }

        AudioDetailVO audioDetailVO = audioService.getAudioDetail(id);
        return Result.success(audioDetailVO);
    }
}