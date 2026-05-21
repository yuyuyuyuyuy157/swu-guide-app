package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nav.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {

    /**
     * 🗺️ 核心地理围栏算法（对应接口文档 2.1）
     * 痛点驱动：根据当前经纬度，利用空间索引高速筛选出“用户当前已经身处哪个景点的电子围栏（半径）内”
     * 核心函数：ST_Distance_Sphere 计算两点球面距离（米）
     */
    @Select("SELECT *, ST_Distance_Sphere(location, ST_GeomFromText(CONCAT('POINT(', #{lng}, ' ', #{lat}, ')'), 4326)) AS distance " +
            "FROM scenic_spots " +
            "WHERE is_deleted = 0 " +
            "AND ST_Distance_Sphere(location, ST_GeomFromText(CONCAT('POINT(', #{lng}, ' ', #{lat}, ')'), 4326)) <= radius " +
            "ORDER BY distance ASC LIMIT 1")
    ScenicSpot findCurrentSpotByLocation(@Param("lng") BigDecimal lng, @Param("lat") BigDecimal lat);

    /**
     * 🏗️ 空间字段专用写入方法
     * 痛点驱动：因为 location 字段是 POINT 类型，需要用这个特殊方法完成带有 SRID 4326 坐标系的动态插入
     */
    @Select("INSERT INTO scenic_spots (name, description, image_url, audio_url, latitude, longitude, location, radius, updated_by) " +
            "VALUES (#{spot.name}, #{spot.description}, #{spot.imageUrl}, #{spot.audioUrl}, #{spot.latitude}, #{spot.longitude}, " +
            "ST_GeomFromText(CONCAT('POINT(', #{spot.longitude}, ' ', #{spot.latitude}, ')'), 4326), #{spot.radius}, #{spot.updatedBy})")
    void insertWithLocation(@Param("spot") ScenicSpot spot);
}