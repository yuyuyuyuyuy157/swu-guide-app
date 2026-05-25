package com.nav.service;

import com.nav.dto.CurrentLocationDTO;
import com.nav.vo.ScenicSpotVO;
import java.util.List;
import com.nav.result.PageResult;
import com.nav.dto.ScenicSpotPageQueryDTO;
public interface ScenicSpotService {
    //获取当前位置景点信息
    ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO);
    //获取全部景点列表信息
    List<ScenicSpotVO> listAllScenicSpots();
    //
    PageResult searchPage(ScenicSpotPageQueryDTO pageQueryDTO);
}