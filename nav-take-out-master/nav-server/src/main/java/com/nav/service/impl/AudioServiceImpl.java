package com.nav.service.impl;

import com.nav.context.BaseContext;
import com.nav.dto.AudioReportProgressDTO;
import com.nav.dto.AudioSettingsDTO;
import com.nav.entity.ScenicSpot;
import com.nav.mapper.ScenicSpotMapper;
import com.nav.service.AudioService;
import com.nav.vo.AudioDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    public AudioDetailVO getAudioDetail(String audioId) {
        Long spotId = Long.valueOf(audioId);
        List<ScenicSpot> allSpots = scenicSpotMapper.listAll();
        ScenicSpot target = allSpots.stream()
                .filter(s -> s.getId().toString().equals(audioId))
                .findFirst()
                .orElse(null);

        if (target == null || target.getAudioUrl() == null || target.getAudioUrl().isBlank()) {
            throw new RuntimeException("This scenic spot has no audio guide");
        }

        Integer lastProgress = 0;
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT play_count FROM user_playback_history WHERE user_id = ? AND spot_id = ?",
                    BaseContext.getCurrentId(), spotId);
            if (!rows.isEmpty()) {
                Object count = rows.get(0).get("play_count");
                lastProgress = count != null ? ((Number) count).intValue() : 0;
            }
        } catch (Exception e) {
            log.warn("Failed to query playback history: {}", e.getMessage());
        }

        return AudioDetailVO.builder()
                .audioUrl(target.getAudioUrl())
                .duration(0)
                .title(target.getName())
                .lastProgress(lastProgress > 0 ? 0 : 0)
                .build();
    }

    @Override
    public void reportProgress(AudioReportProgressDTO dto) {
        Long spotId = Long.valueOf(dto.getAudioId());
        boolean isComplete = dto.getIsComplete() != null && dto.getIsComplete();

        jdbcTemplate.update(
                "INSERT INTO user_playback_history (user_id, spot_id, play_count, last_triggered_at) " +
                        "VALUES (?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE play_count = play_count + 1, last_triggered_at = NOW()",
                BaseContext.getCurrentId(), spotId, isComplete ? 1 : 0);
    }

    @Override
    public void saveSettings(Long userId, AudioSettingsDTO dto) {
        jdbcTemplate.update(
                "INSERT INTO user_audio_settings (user_id, auto_play_enabled, repeat_policy, switch_policy, " +
                        "background_play_enabled, default_speed, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "auto_play_enabled = VALUES(auto_play_enabled), " +
                        "repeat_policy = VALUES(repeat_policy), " +
                        "switch_policy = VALUES(switch_policy), " +
                        "background_play_enabled = VALUES(background_play_enabled), " +
                        "default_speed = VALUES(default_speed), " +
                        "updated_at = NOW()",
                userId,
                dto.getAutoPlay() == null || dto.getAutoPlay() ? 1 : 0,
                dto.getRepeatMode() != null ? dto.getRepeatMode() : 1,
                dto.getPlaySwitchMode() != null ? dto.getPlaySwitchMode() : 1,
                dto.getBackgroundPlay() != null && dto.getBackgroundPlay() ? 1 : 0,
                dto.getPlaySpeed() != null ? dto.getPlaySpeed() : 1.0);
    }

    @Override
    public AudioSettingsDTO getSettings(Long userId) {
        AudioSettingsDTO dto = new AudioSettingsDTO();
        applyDefaultSettings(dto);

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM user_audio_settings WHERE user_id = ?", userId);
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                dto.setAutoPlay(toBool(row.get("auto_play_enabled")));
                dto.setRepeatMode(toInt(row.get("repeat_policy"), 1));
                dto.setPlaySwitchMode(toInt(row.get("switch_policy"), 1));
                dto.setBackgroundPlay(toBool(row.get("background_play_enabled")));
                dto.setPlaySpeed(toDouble(row.get("default_speed"), 1.0));
                dto.setBackwardForwardDuration(15);
            }
        } catch (Exception e) {
            log.warn("Failed to query audio settings: {}", e.getMessage());
        }

        return dto;
    }

    private void applyDefaultSettings(AudioSettingsDTO dto) {
        dto.setAutoPlay(true);
        dto.setRepeatMode(1);
        dto.setPlaySwitchMode(1);
        dto.setBackgroundPlay(false);
        dto.setPlaySpeed(1.0);
        dto.setBackwardForwardDuration(15);
    }

    private Boolean toBool(Object val) {
        if (val == null) return false;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return false;
    }

    private Integer toInt(Object val, int defaultVal) {
        if (val == null) return defaultVal;
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultVal;
    }

    private Double toDouble(Object val, double defaultVal) {
        if (val == null) return defaultVal;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return defaultVal;
    }
}
