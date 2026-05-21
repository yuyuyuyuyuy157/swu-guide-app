package com.nav.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.locationtech.jts.geom.Point; // 引入JTS的空间点对象
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("scenic_spots")
public class ScenicSpot {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;
    private String audioUrl;

    // 界面渲染展示用的基础经纬度
    private BigDecimal latitude;
    private BigDecimal longitude;

    /**
     * 🎯 空间索引核心字段
     * insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER
     * 痛点驱使：因为 MySQL 空间字段写入必须包裹 ST_GeomFromText('POINT(经度 纬度)', 4326) 函数，
     * 单纯靠 MP 自动生成的常规 SQL 无法直接写入，后续我们会提供专门的空间写入 Mapper 方法。
     */
    @TableField(value = "location", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private transient Point location; // transient 避免 MP 默认的常规 CRUD 序列化冲突

    private Integer radius;
    private Long updatedBy;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}