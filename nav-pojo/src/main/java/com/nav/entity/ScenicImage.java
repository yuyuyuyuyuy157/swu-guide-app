package com.nav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 🎯 景点轮播图实体类
 * 对应数据库表: scenic_images
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scenic_images")
public class ScenicImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 自增主金
    private Long id;

    private Long spotId;     // 关联的景点ID
    private String imageUrl;   // 图片存储的绝对或相对URL路径
}