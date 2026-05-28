package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 🎯 用户端 - 自动上报播放进度 DTO
 */
@Data
@Schema(description = "自动上报播放进度参数")
public class AudioProgressDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "语音唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String audioId;

    @Schema(description = "当前播放进度（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer progress;

    // 🎯 新增字段：交给 Lombok 自动生成 getComplete()
    @Schema(description = "是否播放完成", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean complete;
}