package com.nav.service.impl;

import com.nav.entity.ScenicSpot;
import com.nav.mapper.AdminScenicSpotMapper;
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
        log.info("🎯 开始装配景点语音流。步骤一：检查数据库缓存，ID: {}", audioId);

        // 1. 捞取景点主表数据
        ScenicSpot scenicSpot = adminScenicSpotMapper.getById(audioId);
        if (scenicSpot == null) {
            throw new com.nav.exception.BaseException("未找到相关景点的导览数据");
        }

        String audioUrl = scenicSpot.getAudioUrl();
        int estimatedDuration = 0;

        // 2. 🎯 核心看点：懒加载与自动回填缓存防线
        if (audioUrl == null || audioUrl.isBlank()) {
            log.warn("⚠️ 检测到景点 [{}] 尚未生成过语音解说音频，正在强制启动 AI 现场转译...", scenicSpot.getName());

            // 抓取介绍长文本作为台词
            String textToConvert = scenicSpot.getDescription();
            if (textToConvert == null || textToConvert.isBlank()) {
                textToConvert = "欢迎来到美丽的" + scenicSpot.getName() + "。祝您游览愉快！"; // 兜底文案
            }

            // 现场生成物理音频文件并获取 URL
            audioUrl = ttsUtil.convertTextToSpeech(textToConvert);

            // 🌟 自动回填持久化：更新主表中的 audio_url 字段，下个同学路过此景点直接吃缓存，实现秒级响应
            ScenicSpot updateSpot = new ScenicSpot();
            updateSpot.setId(audioId);
            updateSpot.setAudioUrl(audioUrl);
            adminScenicSpotMapper.update(updateSpot); // 复用你的动态更新能力

            log.info("🔑 AI 新合成的音频 URL 已成功反向回填至数据库主表，缓存已激活。");

            // 简单估算时长（成年人平均语速每秒约 4 个字）
            estimatedDuration = textToConvert.length() / 4;
        } else {
            log.info("✅ 命中数据库缓存，音频链接已存在，直接下发。");
            // 若有真实音频，这里可留空或由前端播放器组件动态加载 duration
            estimatedDuration = 60;
        }

        // 3. 严格按照接口文档报文契约组装视图对象返回
        return AudioDetailVO.builder()
                .audioUrl(audioUrl)
                .title(scenicSpot.getName() + " - 官方语音解说")
                .duration(estimatedDuration > 0 ? estimatedDuration : 30)
                .lastProgress(0) // 默认从头开始听
                .build();
    }
}