package com.nav.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 🎯 用户端 - 景点详情完整视图对象
 * 严格对齐前端开发指南 8.2 节的 JSON 报文契约
 */
@Data
public class ScenicSpotDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;               // 景点唯一ID（String化防止前端 JS 丢失 Long 型精度）
    private String name;             // 景点名称
    private String description;      // 完整的长文本介绍（含多段落）
    private String imageUrl;         // 封面图 URL
    private List<String> images;     // 🎯 核心：轮播图 URL 数组
    private String audioUrl;         // 语音讲解 MP3 播放地址
    private BigDecimal latitude;     // 纬度坐标
    private BigDecimal longitude;    // 经度坐标
    private Integer radius;          // 触发自动播报的电子围栏半径（米）
}