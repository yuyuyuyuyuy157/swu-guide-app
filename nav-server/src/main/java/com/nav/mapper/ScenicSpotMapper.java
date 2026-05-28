package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nav.entity.ScenicSpot;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.vo.AdminScenicSpotVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

@Mapper
public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {

    /**
     * 1. 根据当前位置，查询距离最近且在感应范围内的景点
     */
    ScenicSpot getByCurrentLocation(@Param("longitude") BigDecimal longitude, @Param("latitude") BigDecimal latitude);

    /**
     * 2. 条件分页查询（管理端展示）
     * 提示：PageHelper 会自动拦截这个返回为 Page 的方法，并在底层切入 COUNT 语句
     */
    Page<AdminScenicSpotVO> pageQuery(ScenicSpotPageQueryDTO pageQueryDTO);

}