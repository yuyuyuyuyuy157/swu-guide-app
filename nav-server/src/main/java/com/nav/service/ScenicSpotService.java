package com.nav.service;

import com.nav.dto.CurrentLocationDTO;
import com.nav.vo.ScenicSpotDetailVO;
import com.nav.vo.ScenicSpotVO;
import java.util.List;
import com.nav.result.PageResult;
import com.nav.dto.ScenicSpotPageQueryDTO;
public interface ScenicSpotService {
    //获取当前位置景点信息
    ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO);
    //获取全部景点列表信息
    List<ScenicSpotVO> listAllScenicSpots();
    //分页搜索景点
    PageResult searchPage(ScenicSpotPageQueryDTO pageQueryDTO);
    //根据ID查询景点详细信息
    ScenicSpotVO getScenicById(Long scenicId);
    //根据ID查询景点详细信息（包含关联路径锚点等核心内容）
    ScenicSpotDetailVO getDetailById(Long id);
}