package com.nav.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class ScenicSpotVO implements Serializable {
    private String scenicId;      // 景点唯一ID（对应数据库BIGINT，转成String防前端精度丢失）
    private String name;          // 景点名称
    private String image;         // 景点封面图URL
    private String intro;         // 景点简介
    private Boolean hasAudio;     // 是否有语音讲解
    private String audioId;       // 语音讲解ID
    private String distance;      // 距离当前位置
    private Integer inductionRange; // 景点电子围栏感应范围
    private Double latitude;      // 纬度，供前端搜索/地图定位使用
    private Double longitude;     // 经度，供前端搜索/地图定位使用
}
