package com.nav.service;

import com.nav.dto.AudioReportProgressDTO;
import com.nav.dto.AudioSettingsDTO;
import com.nav.vo.AudioDetailVO;

public interface AudioService {
    AudioDetailVO getAudioDetail(String audioId);
    void reportProgress(AudioReportProgressDTO dto);
    void saveSettings(Long userId, AudioSettingsDTO dto);
    AudioSettingsDTO getSettings(Long userId);
}
