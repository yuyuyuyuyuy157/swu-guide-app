package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "用户保存播放设置参数")
public class UserAudioSettingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否开启自动播放", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean autoPlay;

    @Schema(description = "重复模式：1-播一次 2-播多次", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer repeatMode;

    @Schema(description = "切换模式：1-播完再切 2-实时切 3-弹窗", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer playSwitchMode;

    @Schema(description = "是否开启后台播放", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean backgroundPlay;

    @Schema(description = "播放倍速 0.5/1.0/1.2/1.5/2.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Float playSpeed;

    @Schema(description = "快进/快退时长", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer backwardForwardDuration;

    @Schema(description = "防重放UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;
}