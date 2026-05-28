package com.nav.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point; // 引入JTS的空间点对象
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic_spots")
public class ScenicSpot implements Serializable { // 🎯 修正 1：强烈建议实现序列化，契合你前面的 VO/DTO 规范

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 🎯 修正 2：你的 SQL 里是 AUTO_INCREMENT，这里必须改成 AUTO，否则雪花 ID 会冲崩自增主键
    private Long id;

    private String name;
    private String description;
    private String imageUrl;
    private String audioUrl;

    // 界面渲染展示用的基础经纬度
    private BigDecimal latitude;
    private BigDecimal longitude;

    /**
     * 🎯 空间索引核心字段 利用 MP 的 typeHandler 自动解析二进制空间点
     */
    @TableField(value = "location", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Point location;

    private Integer radius;
    private Long updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 🎯 修正 6：根据最新追加的 scenic_images 表，把轮播图列表存放在这里，并标记不存在于主表
     */
    @TableField(exist = false)
    private List<String> images;
}