package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户播放设置")
public class AudioSettingsDTO {

    @Schema(description = "手动开启/关闭自动播放控制")
    private Boolean autoPlay;

    @Schema(description = "重复播放选择：1-每个景点只播放一次, 2-景点可以播放多次")
    private Integer repeatMode;

    @Schema(description = "播放切换模式：1-播完再切, 2-随位置实时切, 3-增加弹窗提醒")
    private Integer playSwitchMode;

    @Schema(description = "是否开启后台播放")
    private Boolean backgroundPlay;

    @Schema(description = "倍速设置：可选值 0.5/1.0/1.2/1.5/2.0")
    private Double playSpeed;

    @Schema(description = "快进/快退时长（秒）：可选值 15/30/60")
    private Integer backwardForwardDuration;
}
