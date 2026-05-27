package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "播放进度上报")
public class AudioReportProgressDTO {
    @Schema(description = "语音ID")
    private String audioId;

    @Schema(description = "当前播放进度（秒）")
    private Integer progress;

    @Schema(description = "是否播放完成")
    private Boolean isComplete;
}
