package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员端 - 景点列表视图对象")
public class AdminScenicSpotVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点ID")
    private String scenicId;

    @Schema(description = "景点名称")
    private String name;

    @Schema(description = "景点简介片段")
    private String intro;

    @Schema(description = "上次修改人姓名")
    private String lastModifier;

    @Schema(description = "上次修改时间", example = "2026-05-28 11:08:00")
    private LocalDateTime modifyTime;
}