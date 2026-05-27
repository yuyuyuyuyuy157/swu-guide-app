package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 🎯 用户音频播放设置提交载荷
 */
@Data
@Schema(description = "用户音频播放设置提交数据")
public class UserAudioSettingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "播放模式：0-随位置切，1-播完再切", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer playMode;

    @Schema(description = "自动播放开关：0-关闭，1-开启", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer autoPlay;
}