package com.nav.service;

import com.nav.vo.AudioDetailVO;

public interface AudioService {
    /**
     * 获取景点语音详情，若无音频则触发 AI 现场即时合成与持久化回填
     * @param audioId 景点ID
     * @return 音频播放载荷
     */
    AudioDetailVO getAudioDetail(Long audioId);
}