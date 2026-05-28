package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自动上报播放进度参数")
public class AudioProgressDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "语音唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "语音唯一ID不能为空") // String 类型必填用 @NotBlank
    private String audioId;

    @Schema(description = "当前播放进度（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "当前播放进度不能为空") // 非 String 类型必填用 @NotNull
    @Min(value = 0, message = "播放进度不能小于0秒") // 额外保障：进度不能是负数
    private Integer progress;

    // 新增字段：交给 Lombok 自动生成 getComplete()
    @Schema(description = "是否播放完成", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    // 非必填字段，不需要加 @NotNull。前端不传时，后端接收到默认为 null
    private Boolean complete;
}