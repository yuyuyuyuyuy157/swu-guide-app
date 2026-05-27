package com.nav.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ScenicSpotDTO implements Serializable {
    private Long id;            // 景点ID（修改时必填）
    private String name;        // 景点名称
    private String description; // 景点详细介绍
    private String imageUrl;    // 封面图URL
    private String audioUrl;    // 语音讲解URL
    private Double latitude;    // 纬度
    private Double longitude;   // 经度
    private Integer radius;     // 电子围栏半径（米）
}