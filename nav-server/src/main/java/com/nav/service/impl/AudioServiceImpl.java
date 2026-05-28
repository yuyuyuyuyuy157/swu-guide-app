package com.nav.service.impl;

import com.nav.dto.UserAudioSettingDTO;
import com.nav.entity.ScenicSpot;
import com.nav.entity.UserPlaybackHistory;
import com.nav.mapper.AdminScenicSpotMapper;
import com.nav.mapper.UserPlaybackHistoryMapper;
import com.nav.service.AudioService;
import com.nav.utils.TtsUtil;
import com.nav.vo.AudioDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AudioServiceImpl implements AudioService {

    @Autowired
    private AdminScenicSpotMapper adminScenicSpotMapper;

    @Autowired
    private TtsUtil ttsUtil;

    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及 AI 转化后回填主表，开启声明式事务控制
    public AudioDetailVO getAudioDetail(Long audioId) {
        log.info("🎯 开始装配景点语音流。步骤一：检查数据库缓存，景点ID: {}", audioId);

        // 1. 捞取景点主表数据
        ScenicSpot scenicSpot = adminScenicSpotMapper.getById(audioId);
        if (scenicSpot == null) {
            throw new com.nav.exception.BaseException("未找到相关景点的导览数据");
        }

        String audioUrl = scenicSpot.getAudioUrl();
        int estimatedDuration = 0;

        // 2. 核心防线：懒加载与自动回填缓存（TTS即时转译）
        if (audioUrl == null || audioUrl.isBlank()) {
            log.warn("⚠️ 检测到景点 [{}] 尚未生成音频，启动 AI 现场转译...", scenicSpot.getName());

            String textToConvert = scenicSpot.getDescription();
            if (textToConvert == null || textToConvert.isBlank()) {
                textToConvert = "欢迎来到美丽的" + scenicSpot.getName() + "。祝您游览愉快！";
            }
            audioUrl = ttsUtil.convertTextToSpeech(textToConvert);

            ScenicSpot updateSpot = new ScenicSpot();
            updateSpot.setId(audioId);
            updateSpot.setAudioUrl(audioUrl);
            adminScenicSpotMapper.update(updateSpot);

            estimatedDuration = textToConvert.length() / 4;
        } else {
            log.info("✅ 命中数据库缓存，直接下发。");
            estimatedDuration = 60;
        }

        // =========================================================
        // 3. 🎯 完美对接你的 UserPlaybackHistory，获取断点进度
        // =========================================================
        Long userId = com.nav.context.BaseContext.getCurrentId();
        Integer savedProgress = 0;

        if (userId != null) {
            // 注意：你的实体里叫 spotId，所以这里传参时把 audioId 作为 spotId 传进去
            UserPlaybackHistory historyRecord = userPlaybackHistoryMapper.selectByUserIdAndSpotId(userId, audioId);

            // 健壮性防线：不仅判断对象不为空，还要防止数据库里 lastProgress 为 NULL 导致拆箱异常
            if (historyRecord != null && historyRecord.getLastProgress() != null) {
                savedProgress = historyRecord.getLastProgress();
                log.info("🎧 检索到用户历史播放记录，累计播放次数: {}，断点位置：{} 秒",
                        historyRecord.getPlayCount(), savedProgress);
            }
        }

        // 4. 组装下发
        return AudioDetailVO.builder()
                .audioUrl(audioUrl)
                .title(scenicSpot.getName() + " - 官方语音解说")
                .duration(estimatedDuration > 0 ? estimatedDuration : 30)
                .lastProgress(savedProgress) // 🎯 完美注入断点进度
                .build();
    }
    @Autowired
    private UserPlaybackHistoryMapper userPlaybackHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 强事务防线
    public void saveOrUpdateProgress(Long userId, Long audioId, Integer progress, Boolean isComplete) {
        log.info("🎯 业务层处理高频上报，用户: {}, 语音/景点: {}, 进度: {}秒", userId, audioId, progress);

        // 🎯 修正：利用你设计的联合索引唯一性，精准抓取足迹历史
        com.nav.entity.UserPlaybackHistory history = userPlaybackHistoryMapper.selectByUserIdAndSpotId(userId, audioId);

        if (history == null) {
            // 历史无足迹：首次收听，执行初始化插入
            com.nav.entity.UserPlaybackHistory newHistory = com.nav.entity.UserPlaybackHistory.builder()
                    .userId(userId)
                    .spotId(audioId)
                    .playCount(1) // 首次听，计为 1 次
                    .lastProgress(progress)
                    .lastTriggeredAt(java.time.LocalDateTime.now())
                    .build();
            userPlaybackHistoryMapper.insertHistory(newHistory);
            log.info("🔑 成功为用户建立 [user_playback_history] 首次收听足迹。");
        } else {
            // 历史有足迹：更新最新断点秒数
            history.setLastProgress(progress);
            history.setLastTriggeredAt(java.time.LocalDateTime.now());

            // 配合接口文档 3.2 节：如果前端判定音频已经 100% 播放完成了（is_complete 为 true），
            // 顺手帮用户的累计播放次数 (play_count) +1
            if (isComplete != null && isComplete) {
                history.setPlayCount(history.getPlayCount() + 1);
            }

            userPlaybackHistoryMapper.updateHistory(history);
            log.info("🔑 历史足迹存在，断点续播秒数已成功推移更新。");
        }
    }
    @Autowired
    private UserAudioSettingMapper userAudioSettingMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSettings(Long userId, UserAudioSettingDTO dto) {
        // 将前端传来的 Boolean 转换为数据库的 0/1 状态
        UserAudioSetting setting = UserAudioSetting.builder()
                .userId(userId)
                .autoPlayEnabled(Boolean.TRUE.equals(dto.getAutoPlay()) ? 1 : 0)
                .repeatPolicy(dto.getRepeatMode())
                .switchPolicy(dto.getPlaySwitchMode())
                .backgroundPlayEnabled(Boolean.TRUE.equals(dto.getBackgroundPlay()) ? 1 : 0)
                .defaultSpeed(dto.getPlaySpeed())
                .backwardForwardDuration(dto.getBackwardForwardDuration())
                .updatedAt(LocalDateTime.now())
                .build();

        // 调用 Mapper 执行 Upsert 逻辑
        userAudioSettingMapper.saveOrUpdate(setting);
    }
}