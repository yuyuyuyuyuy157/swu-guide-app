package com.nav.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nav.dto.CurrentLocationDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.entity.ScenicSpot;
import com.nav.exception.BaseException;
import com.nav.mapper.ScenicImageMapper;
import com.nav.mapper.ScenicSpotMapper;
import com.nav.result.PageResult;
import com.nav.service.ScenicSpotService;
import com.nav.vo.ScenicSpotDetailVO;
import com.nav.vo.ScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
// 🎯 继承 ServiceImpl，解锁主表的全部通用查询能力
public class ScenicSpotServiceImpl extends ServiceImpl<ScenicSpotMapper, ScenicSpot> implements ScenicSpotService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private ScenicImageMapper scenicImageMapper;

    @Override
    public ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO) {
        log.info("开始计算当前位置的附近景点: {}", currentLocationDTO);

        if (currentLocationDTO.getLongitude() == null || currentLocationDTO.getLatitude() == null) {
            return null;
        }

        // 🎯 核心修正：转换为 BigDecimal 喂给 Mapper，严格匹配高精度空间地理计算参数
        BigDecimal lng = BigDecimal.valueOf(currentLocationDTO.getLongitude());
        BigDecimal lat = BigDecimal.valueOf(currentLocationDTO.getLatitude());

        // 1. 调用具有地理空间索引计算能力的专用 Mapper 方法
        ScenicSpot scenicSpot = scenicSpotMapper.getByCurrentLocation(lng, lat);

        // 2. 如果没匹配到任何景点的电子围栏，返回 null
        if (scenicSpot == null) {
            log.info("当前位置不在任何景点的感应范围内");
            return null;
        }

        // 3. 封装为前端需要的 VO
        return convertToScenicSpotVO(scenicSpot, null);
    }

    @Override
    public ScenicSpotVO getScenicById(Long scenicId) {
        log.info("🎯 开始组装手动定位景点的视图载荷，目标ID: {}", scenicId);

        // 1. 🎯 完美平替：干掉 adminScenicSpotMapper.getById()，直接调用 MP 原生内置的 getById
        ScenicSpot scenicSpot = this.getById(scenicId);
        if (scenicSpot == null) {
            log.warn("手动定位失败，目标景点在系统中不存在，ID: {}", scenicId);
            throw new BaseException("未找到相关景点定位信息");
        }

        // 2. 转换并补充手动指定特有的文字提示
        return convertToScenicSpotVO(scenicSpot, "0m (手动指定)");
    }

    @Override
    public List<ScenicSpotVO> listAllScenicSpots() {
        // 1. 🎯 完美平替：干掉手写的 listAll()，调用 MP 内置的 list() 传入空的 Wrapper 即代表全量无条件捞取
        List<ScenicSpot> list = this.list();

        // 2. 利用 Stream 流进行优雅的批量 VO 转换
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(spot -> convertToScenicSpotVO(spot, null))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult searchPage(ScenicSpotPageQueryDTO pageQueryDTO) {
        // 1. 开启 PageHelper 分页拦截器
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2. 🎯 架构避坑重构：不再调用后台 B 端专属的多表联查 pageQuery
        // 用户端搜索属于纯净的单表模糊匹配，直接使用 MP 的 LambdaQuery 配合 PageHelper 拦截器拦截
        LambdaQueryWrapper<ScenicSpot> queryWrapper = new LambdaQueryWrapper<ScenicSpot>()
                .like(pageQueryDTO.getKeyword() != null && !pageQueryDTO.getKeyword().isBlank(),
                        ScenicSpot::getName, pageQueryDTO.getKeyword())
                .orderByDesc(ScenicSpot::getUpdatedAt);

        // PageHelper 会动态拦截这一行 MP 的 list 查询，并自动注入 COUNT 语句组装成 Page 对象
        List<ScenicSpot> list = this.list(queryWrapper);
        Page<ScenicSpot> page = (Page<ScenicSpot>) list;

        // 3. 严格按照用户端接口文档响应参数，清洗并组装数据
        List<ScenicSpotVO> voList = new ArrayList<>();
        if (page.getResult() != null) {
            for (ScenicSpot spot : page.getResult()) {
                ScenicSpotVO vo = new ScenicSpotVO();
                vo.setScenicId(spot.getId().toString()); // 防前端精度丢失
                vo.setName(spot.getName());              // 景点名称
                vo.setImage(spot.getImageUrl());         // 景点封面图
                vo.setDistance("未定");                   // 由前端负责利用高德 SDK 动态计算距离并渲染
                voList.add(vo);
            }
        }

        // 4. 返回标准分页结果
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public ScenicSpotDetailVO getDetailById(Long id) {
        log.info("🎯 业务层启动分步查询。阶段一：捞取景点主表元数据，ID: {}", id);

        // 1. 🎯 完美平替：利用 MP 内置主键查询
        ScenicSpot spot = this.getById(id);
        if (spot == null) {
            return null;
        }

        log.info("阶段二：通过外键关联，去副表捞取该景点的全部轮播图片");
        // 2. 🎯 修正：此处 selectUrlsByScenicId 方法内部已经帮我们对齐了新表的 `spot_id`
        List<String> imageUrlList = scenicImageMapper.selectUrlsByScenicId(id);

        // 3. 契约聚合
        ScenicSpotDetailVO detailVO = new ScenicSpotDetailVO();
        BeanUtils.copyProperties(spot, detailVO);

        // 特殊属性精准修正与二次清洗
        detailVO.setId(spot.getId().toString()); // 黄金防护：长整型 String 化防止前端 JS 精度截断
        detailVO.setImages(imageUrlList);        // 塞入多媒体轮播图链接集合

        log.info("景点详情 VO 组装大功告成。名称: {}, 共加载轮播图 {} 张", detailVO.getName(), imageUrlList.size());
        return detailVO;
    }

    /**
     * 💡 提取公共清洗逻辑：将 ScenicSpot 实体类优雅转化为前端视角的 ScenicSpotVO
     */
    private ScenicSpotVO convertToScenicSpotVO(ScenicSpot spot, String overrideDistance) {
        ScenicSpotVO vo = new ScenicSpotVO();
        vo.setScenicId(spot.getId().toString()); // 规范化 String 处理防止前端长整型精度截断
        vo.setName(spot.getName());
        vo.setImage(spot.getImageUrl());
        vo.setIntro(spot.getDescription());
        vo.setInductionRange(spot.getRadius());

        // 语音讲解关联性深度计算
        boolean hasAudio = spot.getAudioUrl() != null && !spot.getAudioUrl().isBlank();
        vo.setHasAudio(hasAudio);
        vo.setAudioId(hasAudio ? spot.getId().toString() : null);

        // 距离赋值策略
        if (overrideDistance != null) {
            vo.setDistance(overrideDistance);
        }

        return vo;
    }
}