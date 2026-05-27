package com.nav.mapper;

import com.nav.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScenicSpotMapper {
    ScenicSpot getByCurrentLocation(@Param("longitude") Double longitude, @Param("latitude") Double latitude);

    List<ScenicSpot> listAll();

    List<ScenicSpot> searchByKeyword(@Param("keyword") String keyword);
}
