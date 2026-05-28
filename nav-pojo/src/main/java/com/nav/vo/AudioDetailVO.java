package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Schema(description = "景点语音详情视图对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "语音播放地址")
    private String audioUrl;

    @Schema(description = "语音总时长（秒）")
    private Integer duration;

    @Schema(description = "语音标题")
    private String title;

    @Schema(description = "上次播放进度（断点续播秒数，无记录则返回0）")
    private Integer lastProgress;
}