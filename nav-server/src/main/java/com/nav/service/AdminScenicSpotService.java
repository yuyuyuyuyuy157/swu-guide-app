package com.nav.service;

import com.nav.dto.ScenicSpotDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.result.PageResult;
import java.util.List;
import com.nav.entity.ScenicSpot;

public interface AdminScenicSpotService {
    //新增景点
    void saveWithFields(ScenicSpotDTO scenicSpotDTO);
    //根据ID查询景点详细数据
    ScenicSpot getById(Long id);
    //修改景点核心内容及关联路径锚点
    void updateWithRoute(ScenicSpotDTO scenicSpotDTO);

    void deleteBatch(List<Long> ids);
    //搜索景点
    PageResult pageQuery(ScenicSpotPageQueryDTO pageQueryDTO);
}