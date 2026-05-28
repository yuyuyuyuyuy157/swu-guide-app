package com.nav.controller;

import com.nav.context.BaseContext;
import com.nav.dto.AudioProgressDTO;
import com.nav.dto.UserAudioSettingDTO;
import com.nav.result.Result;
import com.nav.service.AudioService;
import com.nav.vo.AudioDetailVO;
import com.nav.vo.UserAudioSettingVO;
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

    /**
     * 3.2 自动上报播放进度
     * 接口路径：POST /api/v1/audio/progress
     * 业务规范：前端通过节流阀（如每5秒或在 pause/destroy 时）触发上报
     * @param progressDTO 进度参数
     * @return 统一返回结果
     */
    @PostMapping("/progress")
    @Operation(summary = "自动上报播放进度")
    public Result<String> reportProgress(@RequestBody AudioProgressDTO progressDTO) {
        // 从当前请求线程的 ThreadLocal 中捞取已登录的用户 ID
        Long userId = BaseContext.getCurrentId();
        log.info("📡 自动上报进度流触发，用户ID: {}, 语音ID: {}, 当前时间点: {}秒",
                userId, progressDTO.getAudioId(), progressDTO.getProgress());

        // 参数合法性边界控制
        if (progressDTO.getAudioId() == null || progressDTO.getProgress() == null) {
            return Result.error(400, "上报失败：音频ID和进度秒数不能为空");
        }
        if (progressDTO.getProgress() < 0) {
            return Result.error(400, "非法的播放进度时间");
        }

        Long audioId;
        try {
            audioId = Long.valueOf(progressDTO.getAudioId());
        } catch (NumberFormatException e) {
            return Result.error(400, "音频ID格式非法");
        }

        // 调度业务层执行“存在则更新，不存在则插入”的原子操作
        audioService.saveOrUpdateProgress(userId, audioId, progressDTO.getProgress(), progressDTO.getComplete());
        return Result.success("进度保存成功");
    }
    /**
     * 3.3 保存用户播放设置
     * 接口路径：POST /api/v1/audio/save-settings
     */
    @PostMapping("/save-settings")
    @Operation(summary = "保存用户播放设置")
    public Result<String> saveSettings(@RequestBody UserAudioSettingDTO settingDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("📡 收到保存播放设置请求，用户ID: {}, RequestID: {}", userId, settingDTO.getRequestId());

        // 可选：利用 Redis 检查 requestId 是否在近期处理过（严格防重方案）。
        // 由于这是状态覆盖操作，依赖数据库层的唯一键冲突更新已经足够满足幂等。
        audioService.saveSettings(userId, settingDTO);

        return Result.success();
    }

    @GetMapping("/get-settings")
    @Operation(summary = "获取用户播放设置")
    public Result<UserAudioSettingVO> getSettings() {
        // 从当前请求线程上下文中安全捞出登录用户的唯一ID
        Long userId = BaseContext.getCurrentId();
        log.info("📡 收到获取播放设置请求，用户ID: {}", userId);

        UserAudioSettingVO settingVO = audioService.getSettings(userId);
        return Result.success(settingVO);
    }
}