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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Value("${nav.upload.base-dir:D:/nav-uploads}")
    private String uploadBaseDir;

    @Value("${nav.tts.enabled:true}")
    private boolean ttsEnabled;

    @Value("${nav.tts.powershell-path:C:/Windows/System32/WindowsPowerShell/v1.0/powershell.exe}")
    private String powerShellPath;

    @Override
    public AudioDetailVO getAudioDetail(String audioId) {
        Long spotId = Long.valueOf(audioId);
        List<ScenicSpot> allSpots = scenicSpotMapper.listAll();
        ScenicSpot target = allSpots.stream()
                .filter(s -> s.getId().toString().equals(audioId))
                .findFirst()
                .orElse(null);

        if (target == null) {
            throw new RuntimeException("This scenic spot does not exist");
        }

        String audioUrl = target.getAudioUrl();
        if (audioUrl == null || audioUrl.isBlank()) {
            audioUrl = generateNarrationAudio(target);
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
                .audioUrl(audioUrl)
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
        if (val instanceof Boolean) return (Boolean) val;
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

    private String generateNarrationAudio(ScenicSpot spot) {
        if (!ttsEnabled) {
            throw new RuntimeException("Server TTS is disabled");
        }

        String text = buildNarrationText(spot);
        if (text.isBlank()) {
            throw new RuntimeException("This scenic spot has no narration text");
        }

        try {
            String hash = sha256(text).substring(0, 16);
            String fileName = "scenic-" + spot.getId() + "-" + hash + ".wav";
            Path dir = Path.of(uploadBaseDir, "tts");
            Path audioFile = dir.resolve(fileName);

            if (Files.exists(audioFile) && Files.size(audioFile) > 44) {
                return "/download/tts/" + fileName;
            }

            Files.createDirectories(dir);
            synthesizeWithWindowsSapi(text, audioFile);
            return "/download/tts/" + fileName;
        } catch (Exception e) {
            log.error("Failed to generate narration audio for scenicId={}: {}", spot.getId(), e.getMessage());
            throw new RuntimeException("Failed to generate narration audio");
        }
    }

    private String buildNarrationText(ScenicSpot spot) {
        String name = spot.getName() == null ? "" : spot.getName().trim();
        String description = spot.getDescription() == null ? "" : spot.getDescription().trim();
        String text = (name + "。" + description).trim();
        return text.length() > 1500 ? text.substring(0, 1500) : text;
    }

    private void synthesizeWithWindowsSapi(String text, Path outputFile) throws Exception {
        String script = """
                Add-Type -AssemblyName System.Speech
                $text = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String('__TEXT__'))
                $out = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String('__OUTPUT__'))
                $tmp = $out + '.tmp.wav'
                if (Test-Path $tmp) { Remove-Item $tmp -Force }
                $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
                $voice = $synth.GetInstalledVoices() |
                  Where-Object {
                    $_.Enabled -and (
                      $_.VoiceInfo.Culture.Name -like 'zh*' -or
                      $_.VoiceInfo.Name -match 'Chinese|Huihui|Kangkang|Yaoyao|Xiaoxiao|Yunxi|Xiaoyi'
                    )
                  } |
                  Select-Object -First 1
                if ($null -ne $voice) { $synth.SelectVoice($voice.VoiceInfo.Name) }
                $synth.Rate = 0
                $synth.Volume = 100
                $synth.SetOutputToWaveFile($tmp)
                $synth.Speak($text)
                $synth.SetOutputToNull()
                $synth.Dispose()
                Move-Item -Path $tmp -Destination $out -Force
                """;

        script = script
                .replace("__TEXT__", Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)))
                .replace("__OUTPUT__", Base64.getEncoder().encodeToString(outputFile.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)));

        String encodedCommand = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        Process process = new ProcessBuilder(
                powerShellPath,
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass",
                "-EncodedCommand",
                encodedCommand
        ).redirectErrorStream(true).start();

        boolean finished = process.waitFor(Duration.ofSeconds(45).toMillis(), TimeUnit.MILLISECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("PowerShell TTS timed out");
        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("PowerShell TTS failed: " + output);
        }
        if (!Files.exists(outputFile) || Files.size(outputFile) <= 44) {
            throw new RuntimeException("PowerShell TTS produced an empty file");
        }
    }

    private String sha256(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
