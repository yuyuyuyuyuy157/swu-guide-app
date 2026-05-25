package com.nav.service;

import com.nav.dto.ScenicSpotDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.result.PageResult;
import java.util.List;

public interface AdminScenicSpotService {
    void saveWithFields(ScenicSpotDTO scenicSpotDTO);
    ScenicSpotDTO getById(Long id);
    void updateWithFields(ScenicSpotDTO scenicSpotDTO);
    void deleteBatch(List<Long> ids);
    //搜索景点
    PageResult pageQuery(ScenicSpotPageQueryDTO pageQueryDTO);
}