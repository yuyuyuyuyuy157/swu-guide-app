package com.nav.service;

import com.nav.dto.CurrentLocationDTO;
import com.nav.vo.ScenicSpotVO;
import java.util.List;
public interface ScenicSpotService {
    ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO);
    List<ScenicSpotVO> listAllScenicSpots();
}