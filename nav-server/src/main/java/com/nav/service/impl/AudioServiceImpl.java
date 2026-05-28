package com.nav.service.impl;

import com.nav.context.BaseContext;
import com.nav.dto.UserAudioSettingDTO;
import com.nav.entity.ScenicSpot;
import com.nav.entity.UserAudioSetting;
import com.nav.entity.UserPlaybackHistory;
import com.nav.exception.BaseException;
import com.nav.mapper.AdminScenicSpotMapper;
import com.nav.mapper.ScenicSpotMapper;
import com.nav.mapper.UserAudioSettingMapper;
import com.nav.mapper.UserPlaybackHistoryMapper;
import com.nav.service.AudioService;
import com.nav.utils.TtsUtil;
import com.nav.vo.AudioDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AudioServiceImpl implements AudioService {

    @Autowired
    private AdminScenicSpotMapper adminScenicSpotMapper;

    @Autowired
    private TtsUtil ttsUtil;
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;
    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及 AI 转化后回填主表，开启声明式事务控制
    public AudioDetailVO getAudioDetail(Long audioId) {
        log.info("开始装配景点语音流。步骤一：检查数据库缓存，景点ID: {}", audioId);

        // 1. 从当前线程中捞取用户 ID（因为该接口需要登录权限）
        Long userId = BaseContext.getCurrentId();
        log.info("开始查询语音详情，用户ID: {}, 语音ID(景点ID): {}", userId, audioId);

        // 2. 查询景点的基本音频信息
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(audioId);
        if (scenicSpot == null) {
            throw new BaseException("目标景点或语音资源不存在");
        }

        // 3. 查询该用户针对该景点的断点续播足迹数据
        UserPlaybackHistory history = userPlaybackHistoryMapper.getByUserIdAndSpotId(userId, audioId);

        // 4. 如果有历史记录就取上次的进度，没有就从 0 秒开始首播
        Integer lastProgress = (history != null) ? history.getLastProgress() : 0;

        // 5. 拼装对齐《接口文档5.18.1》3.1节的契约数据
        return AudioDetailVO.builder()
                .audioUrl(scenicSpot.getAudioUrl())
                // 假设你的景点表里还没有存储音频总时长，这里可以先从实体取，或者结合你的AI TTS懒加载技术动态获取
                .duration(300) // 示例伪数据，实际开发中可以从数据库读取独立音频时长字段
                .title(scenicSpot.getName() + "语音讲解")
                .lastProgress(lastProgress)
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