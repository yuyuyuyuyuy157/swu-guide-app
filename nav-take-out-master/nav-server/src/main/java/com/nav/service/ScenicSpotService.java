package com.nav.service;

import com.nav.dto.CurrentLocationDTO;
import com.nav.vo.ScenicSpotVO;
import com.nav.vo.ScenicSpotLocationVO;

import java.util.List;

public interface ScenicSpotService {
    ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO);
    List<ScenicSpotVO> listAllScenicSpots();
    List<ScenicSpotVO> searchScenicSpots(String keyword);
    ScenicSpotLocationVO getScenicLocation(String scenicId);
}
