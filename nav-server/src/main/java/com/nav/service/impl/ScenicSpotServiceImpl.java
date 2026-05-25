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
import com.nav.result.PageResult;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.mapper.AdminScenicSpotMapper;
@Service
@Slf4j
public class ScenicSpotServiceImpl implements ScenicSpotService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;
    private AdminScenicSpotMapper adminScenicSpotMapper;
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
    public ScenicSpotVO getScenicById(Long scenicId) {
        log.info("🎯 开始组装手动定位景点的视图载荷，目标ID: {}", scenicId);

        // 1. 复用持久层接口，捞出未被软删除的有效景点元数据
        ScenicSpot scenicSpot = adminScenicSpotMapper.getById(scenicId);
        if (scenicSpot == null) {
            log.warn("手动定位失败，目标景点在系统中不存在或已被软删除，ID: {}", scenicId);
            throw new com.nav.exception.BaseException("未找到相关景点定位信息");
        }

        // 2. 深度重组清洗为符合《接口文档5.18.1》规范的 VO 视图对象
        ScenicSpotVO vo = new ScenicSpotVO();
        vo.setScenicId(scenicSpot.getId().toString()); // 黄金防线：规范化String处理防止前端长整型精度截断
        vo.setName(scenicSpot.getName());
        vo.setImage(scenicSpot.getImageUrl());
        vo.setIntro(scenicSpot.getDescription());
        vo.setInductionRange(scenicSpot.getRadius());

        // 3. 语音讲解关联性深度计算
        boolean hasAudio = scenicSpot.getAudioUrl() != null && !scenicSpot.getAudioUrl().isBlank();
        vo.setHasAudio(hasAudio);
        vo.setAudioId(hasAudio ? scenicSpot.getId().toString() : null);

        // 4. 降级定位策略优化：由于用户是主动点选了该景点，因此直线物理距离在视觉层面上直接归零呈现
        vo.setDistance("0m (手动指定)");

        log.info("手动定位视图载荷组装完毕：景点名称={}, 语音状态={}", vo.getName(), vo.getHasAudio());
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
    //用户端搜索
    @Override
    public PageResult searchPage(ScenicSpotPageQueryDTO pageQueryDTO) {
        // 1. 开启 PageHelper 分页拦截器
        com.github.pagehelper.PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2. 执行用户端高性能专属查询
        com.github.pagehelper.Page<com.nav.entity.ScenicSpot> page = scenicSpotMapper.pageQuery(pageQueryDTO);

        // 3. 严格按照用户端接口文档响应参数，清洗并组装数据
        List<ScenicSpotVO> voList = new ArrayList<>();
        if (page.getResult() != null && !page.getResult().isEmpty()) {
            for (ScenicSpot spot : page.getResult()) {
                ScenicSpotVO vo = new ScenicSpotVO();

                // 严格对齐文档字段
                vo.setScenicId(spot.getId().toString()); // 防前端精度丢失
                vo.setName(spot.getName());              // 景点名称
                vo.setImage(spot.getImageUrl());         // 景点封面图

                // 💡 进阶亮点：此处 distance 可以先设置为 "计算中"
                // 或者由前端高德地图拿到这一页的坐标后，在本地浏览器批量计算当前定位到这些景点的真实距离
                vo.setDistance("未定");

                voList.add(vo);
            }
        }

        // 4. 返回标准分页结果
        return new PageResult(page.getTotal(), voList);
    }
}