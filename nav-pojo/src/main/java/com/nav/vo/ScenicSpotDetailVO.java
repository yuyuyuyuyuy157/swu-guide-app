package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Schema(description = "景点详情完整视图对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpotDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "景点唯一ID（String化防止前端 JS 丢失 Long 型精度）")
    private String id;

    @Schema(description = "景点名称")
    private String name;

    @Schema(description = "完整的长文本介绍（含多段落）")
    private String description;

    @Schema(description = "封面图 URL")
    private String imageUrl;

    @Schema(description = "轮播图 URL 数组")
    private List<String> images;

    @Schema(description = "语音讲解 MP3 播放地址")
    private String audioUrl;

    @Schema(description = "纬度坐标")
    private BigDecimal latitude;

    @Schema(description = "经度坐标")
    private BigDecimal longitude;

    @Schema(description = "触发自动播报的电子围栏半径（米）")
    private Integer radius;
}