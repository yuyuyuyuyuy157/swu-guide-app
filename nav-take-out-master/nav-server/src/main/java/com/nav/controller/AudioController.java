package com.nav.controller;

import com.nav.context.BaseContext;
import com.nav.dto.AudioReportProgressDTO;
import com.nav.dto.AudioSettingsDTO;
import com.nav.result.Result;
import com.nav.service.AudioService;
import com.nav.vo.AudioDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/audio")
@Tag(name = "音频播放模块接口")
public class AudioController {

    @Autowired
    private AudioService audioService;

    @GetMapping("/detail")
    @Operation(summary = "获取景点语音详情")
    public Result<AudioDetailVO> getAudioDetail(@RequestParam String audioId) {
        log.info("请求音频详情，audioId: {}", audioId);
        AudioDetailVO detail = audioService.getAudioDetail(audioId);
        return Result.success(detail);
    }

    @PostMapping("/report-progress")
    @Operation(summary = "上报播放进度")
    public Result<Void> reportProgress(@RequestBody AudioReportProgressDTO dto) {
        log.info("上报播放进度: audioId={}, progress={}, isComplete={}",
                dto.getAudioId(), dto.getProgress(), dto.getIsComplete());
        audioService.reportProgress(dto);
        return Result.success(null);
    }

    @PostMapping("/save-settings")
    @Operation(summary = "保存用户播放设置")
    public Result<Void> saveSettings(@RequestBody AudioSettingsDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("保存播放设置，userId: {}, settings: {}", userId, dto);
        audioService.saveSettings(userId, dto);
        return Result.success(null);
    }

    @GetMapping("/get-settings")
    @Operation(summary = "获取用户播放设置")
    public Result<AudioSettingsDTO> getSettings() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取播放设置，userId: {}", userId);
        AudioSettingsDTO settings = audioService.getSettings(userId);
        return Result.success(settings);
    }
}
