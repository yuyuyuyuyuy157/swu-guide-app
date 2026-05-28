package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 🎯 用户端 - 手动设置位置请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "手动设置当前位置请求参数")
public class ScenicSpotSetPositionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "手动选择的目标景点唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "选中的景点ID不能为空")
    private String scenicId;
}