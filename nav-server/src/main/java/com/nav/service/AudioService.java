package com.nav.service;

import com.nav.dto.UserAudioSettingDTO;
import com.nav.vo.AudioDetailVO;
import com.nav.vo.UserAudioSettingVO;

public interface AudioService {
    // 获取景点语音详情，若无音频则触发 AI 现场即时合成与持久化回填
    AudioDetailVO getAudioDetail(Long audioId);
    // 保存或更新用户对某一景点的音频播报进度
    void saveOrUpdateProgress(Long userId, Long audioId, Integer progress, Boolean isComplete);
    // 保存用户的播放设置
    void saveSettings(Long userId, UserAudioSettingDTO dto);
    // 获取播放设置
    UserAudioSettingVO getSettings(Long userId);
}