package com.nav.mapper;

import com.nav.dto.CurrentLocationDTO;
import com.nav.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScenicSpotMapper {

    /**
     * 根据当前位置，查询距离最近且在感应范围内的景点
     */
    ScenicSpot getByCurrentLocation(Double longitude, Double latitude);
}