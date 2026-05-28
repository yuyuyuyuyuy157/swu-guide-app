package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 🎯 用户音频播放设置回显视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户音频播放设置回显数据")
public class UserAudioSettingVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否开启自动播放")
    private Boolean autoPlay;

    @Schema(description = "重复模式：1-每个景点只播放一次 2-景点可以播放多次")
    private Integer repeatMode;

    @Schema(description = "播放切换模式：1-播完再切 2-随位置实时切 3-增加弹窗提醒")
    private Integer playSwitchMode;

    @Schema(description = "是否开启后台播放")
    private Boolean backgroundPlay;

    @Schema(description = "倍速设置：可选值 0.5/1.0/1.2/1.5/2.0")
    private Float playSpeed;

    @Schema(description = "快进/快退时长（秒）")
    private Integer backwardForwardDuration;
}