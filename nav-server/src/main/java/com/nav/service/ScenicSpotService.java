package com.nav.service;

import com.nav.dto.CurrentLocationDTO;
import com.nav.vo.ScenicSpotVO;

public interface ScenicSpotService {
    ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO);
}