package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "景点视图对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpotVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "景点唯一ID（对应数据库BIGINT，转成String防前端精度丢失）")
    private String scenicId;

    @Schema(description = "景点名称")
    private String name;

    @Schema(description = "景点封面图URL")
    private String image;

    @Schema(description = "景点简介")
    private String intro;

    @Schema(description = "是否有语音讲解")
    private Boolean hasAudio;

    @Schema(description = "语音讲解ID")
    private String audioId;

    @Schema(description = "距离当前位置")
    private String distance;

    @Schema(description = "景点电子围栏感应范围（米）")
    private Integer inductionRange;
}