package com.nav.mapper;

import com.nav.dto.CurrentLocationDTO;
import com.nav.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.nav.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.github.pagehelper.Page;
import com.nav.dto.ScenicSpotPageQueryDTO;
@Mapper
public interface ScenicSpotMapper {

     // 根据当前位置，查询距离最近且在感应范围内的景点
    ScenicSpot getByCurrentLocation(Double longitude, Double latitude);
    //查询所有未删除的景点（全量查询，供地图初始化渲染使用）
    List<ScenicSpot> listAll();

    Page<ScenicSpot> pageQuery(ScenicSpotPageQueryDTO pageQueryDTO);
}