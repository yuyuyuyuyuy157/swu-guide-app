package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Schema(description = "景点保存/更新数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpotDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "景点ID（修改时必填）")
    // 注意：如果是新增接口则不需要此字段，如果是修改接口建议在 Controller 层配合 @Validated 分组校验，或者由业务逻辑判断
    private Long id;

    @Schema(description = "景点名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "景点名称不能为空")
    private String name;

    @Schema(description = "景点详细介绍", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "景点详细介绍不能为空")
    private String description;

    @Schema(description = "封面图URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "封面图URL不能为空")
    private String imageUrl;

    @Schema(description = "语音讲解URL")
    private String audioUrl;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "纬度不能为空") // String用@NotBlank，其他类型（Double/Integer/Long）用@NotNull
    private Double latitude;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经度不能为空")
    private Double longitude;

    @Schema(description = "电子围栏半径（米）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "电子围栏半径不能为空")
    private Integer radius;

    @Schema(description = "轮播图URL列表")
    private List<String> images;
}