package com.nav.service.impl;

import com.nav.dto.CurrentLocationDTO;
import com.nav.entity.ScenicSpot;
import com.nav.mapper.ScenicSpotMapper;
import com.nav.service.ScenicSpotService;
import com.nav.vo.ScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScenicSpotServiceImpl implements ScenicSpotService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    public ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO) {
        log.info("开始计算当前位置的附近景点: {}", currentLocationDTO);

        // 1. 调用 Mapper 利用空间索引查询
        ScenicSpot scenicSpot = scenicSpotMapper.getByCurrentLocation(
                currentLocationDTO.getLongitude(),
                currentLocationDTO.getLatitude()
        );

        // 2. 如果没匹配到任何景点的电子围栏，按接口规范：data 返回 null
        if (scenicSpot == null) {
            log.info("当前位置不在任何景点的感应范围内");
            return null;
        }

        // 3. 封装为前端需要的 VO
        ScenicSpotVO vo = new ScenicSpotVO();
        vo.setScenicId(scenicSpot.getId().toString()); // 防精度丢失
        vo.setName(scenicSpot.getName());
        vo.setImage(scenicSpot.getImageUrl());
        vo.setIntro(scenicSpot.getDescription());

        // 音频相关逻辑判断
        boolean hasAudio = scenicSpot.getAudioUrl() != null && !scenicSpot.getAudioUrl().isEmpty();
        vo.setHasAudio(hasAudio);
        vo.setAudioId(hasAudio ? scenicSpot.getId().toString() : null); // 如果有音频，关联其音频ID

        // 重新计算一次距离用于展示，或者通过业务再次换算
        // 这里的赋值也可以在SQL中直接查询出距离拿到，此处为简化逻辑映射
        vo.setInductionRange(scenicSpot.getRadius());

        return vo;
    }

    @Override
    public List<ScenicSpotVO> listAllScenicSpots() {
        // 1. 直接查询全量 Entity 列表（不启用 PageHelper）
        List<ScenicSpot> list = scenicSpotMapper.listAll();

        // 2. 批量转化为 VO
        List<ScenicSpotVO> voList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (ScenicSpot spot : list) {
                ScenicSpotVO vo = new ScenicSpotVO();
                vo.setScenicId(spot.getId().toString()); // 规范：String化防精度丢失
                vo.setName(spot.getName());
                vo.setImage(spot.getImageUrl());
                vo.setIntro(spot.getDescription());
                vo.setInductionRange(spot.getRadius());

                // 音频逻辑
                boolean hasAudio = spot.getAudioUrl() != null && !spot.getAudioUrl().isEmpty();
                vo.setHasAudio(hasAudio);
                vo.setAudioId(hasAudio ? spot.getId().toString() : null);

                voList.add(vo);
            }
        }
        return voList;
    }
}