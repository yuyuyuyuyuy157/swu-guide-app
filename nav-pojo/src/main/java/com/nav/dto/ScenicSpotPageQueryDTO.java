package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Schema(description = "景点分页查询条件数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpotPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词（支持匹配景点名称、简介等）")
    private String keyword;

    @Schema(description = "页码（从 1 开始）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小不能小于 1")
    private Integer page;

    @Schema(description = "每页展示的条数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小不能小于 1")
    private Integer pageSize;
}