package com.nav.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nav.context.BaseContext;
import com.nav.dto.UserAudioSettingDTO;
import com.nav.entity.ScenicSpot;
import com.nav.entity.UserAudioSetting;
import com.nav.entity.UserPlaybackHistory;
import com.nav.exception.BaseException;
import com.nav.mapper.ScenicSpotMapper;
import com.nav.mapper.UserAudioSettingMapper;
import com.nav.mapper.UserPlaybackHistoryMapper;
import com.nav.service.AudioService;
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
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private UserPlaybackHistoryMapper userPlaybackHistoryMapper;

    @Autowired
    private UserAudioSettingMapper userAudioSettingMapper;

    @Override
    public AudioDetailVO getAudioDetail(Long audioId) {
        log.info("开始装配景点语音流。步骤一：检查数据库缓存，景点ID: {}", audioId);

        // 1. 从当前线程中捞取用户 ID
        Long userId = BaseContext.getCurrentId();
        log.info("开始查询语音详情，用户ID: {}, 语音ID(景点ID): {}", userId, audioId);

        // 2. 查询景点的基本音频信息（直接利用 MP 内置的 selectById）
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(audioId);
        if (scenicSpot == null) {
            throw new BaseException("目标景点或语音资源不存在");
        }

        // 3. 🎯 完美替代：利用 MP 的 LambdaQueryWrapper 代替已经删除的 selectByUserIdAndSpotId 方法
        UserPlaybackHistory history = userPlaybackHistoryMapper.selectOne(
                new LambdaQueryWrapper<UserPlaybackHistory>()
                        .eq(UserPlaybackHistory::getUserId, userId)
                        .eq(UserPlaybackHistory::getSpotId, audioId)
        );

        // 4. 如果有历史记录就取上次的进度，没有就从 0 秒开始首播
        Integer lastProgress = (history != null) ? history.getLastProgress() : 0;

        // 5. 拼装返回数据
        return AudioDetailVO.builder()
                .audioUrl(scenicSpot.getAudioUrl())
                .duration(300) // 示例数据，实际开发中可扩充数据库字段后读取
                .title(scenicSpot.getName() + "语音讲解")
                .lastProgress(lastProgress)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 强事务防线
    public void saveOrUpdateProgress(Long userId, Long audioId, Integer progress, Boolean isComplete) {
        log.info("🎯 业务层处理高频上报，用户: {}, 语音/景点: {}, 进度: {}秒", userId, audioId, progress);

        // 1. 🎯 完美替代：使用 MP 的 LambdaQueryWrapper 抓取唯一的历史足迹数据
        UserPlaybackHistory history = userPlaybackHistoryMapper.selectOne(
                new LambdaQueryWrapper<UserPlaybackHistory>()
                        .eq(UserPlaybackHistory::getUserId, userId)
                        .eq(UserPlaybackHistory::getSpotId, audioId)
        );

        if (history == null) {
            // 历史无足迹：首次收听，执行初始化插入
            UserPlaybackHistory newHistory = UserPlaybackHistory.builder()
                    .userId(userId)
                    .spotId(audioId)
                    .playCount(1)
                    .lastProgress(progress)
                    .lastTriggeredAt(LocalDateTime.now())
                    .build();

            // 🎯 完美替代：利用 MP 原生内置的单条插入
            userPlaybackHistoryMapper.insert(newHistory);
            log.info("🔑 成功为用户建立 [user_playback_history] 首次收听足迹。");
        } else {
            // 历史有足迹：更新最新断点秒数
            history.setLastProgress(progress);
            history.setLastTriggeredAt(LocalDateTime.now());

            if (isComplete != null && isComplete) {
                history.setPlayCount(history.getPlayCount() + 1);
            }

            // 🎯 完美替代：利用 MP 原生根据主键 ID 更新的方法
            userPlaybackHistoryMapper.updateById(history);
            log.info("🔑 历史足迹存在，断点续播秒数已成功推移更新。");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSettings(Long userId, UserAudioSettingDTO dto) {
        // 1. 🎯 核心修正：构建实体类时，剔除了最新 nav.sql 中已废弃的倍速、快进快退幽灵字段
        UserAudioSetting setting = UserAudioSetting.builder()
                .userId(userId)
                .autoPlayEnabled(Boolean.TRUE.equals(dto.getAutoPlay()) ? 1 : 0)
                .repeatPolicy(dto.getRepeatMode())
                .switchPolicy(dto.getPlaySwitchMode())
                .backgroundPlayEnabled(Boolean.TRUE.equals(dto.getBackgroundPlay()) ? 1 : 0)
                .updatedAt(LocalDateTime.now())
                .build();

        // 2. 🎯 完美平替 ON DUPLICATE KEY UPDATE：
        // 利用 MP 在 Mapper 层提供的高级更新构造器，通过特定的唯一约束组合（或主键）一键进行 Upsert
        userAudioSettingMapper.insertOrUpdate(setting);
        log.info("🔑 用户播放设置（Upsert）保存成功。");
    }
}