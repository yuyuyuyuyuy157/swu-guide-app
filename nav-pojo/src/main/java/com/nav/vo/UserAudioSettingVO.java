package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

/**
 * 🎯 用户音频播放设置回显视图对象
 */
@Data
@Builder
@Schema(description = "用户音频播放设置回显数据")
public class UserAudioSettingVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "播放模式：0-随位置切，1-播完再切")
    private Integer playMode;

    @Schema(description = "自动播放开关：0-关闭，1-开启")
    private Integer autoPlay;
}