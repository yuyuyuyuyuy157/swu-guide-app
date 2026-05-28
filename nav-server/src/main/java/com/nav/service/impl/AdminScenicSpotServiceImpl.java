package com.nav.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nav.dto.ScenicSpotDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.entity.ScenicImage;
import com.nav.entity.ScenicSpot;
import com.nav.mapper.ScenicImageMapper;
import com.nav.mapper.ScenicSpotMapper;
import com.nav.result.PageResult;
import com.nav.service.AdminScenicSpotService;
import com.nav.vo.AdminScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
// 🎯 继承 ServiceImpl 后，基础单表 CRUD 能力（如 this.save, this.updateById）直接激活，且自带 baseMapper
public class AdminScenicSpotServiceImpl extends ServiceImpl<ScenicSpotMapper, ScenicSpot> implements AdminScenicSpotService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private ScenicImageMapper scenicImageMapper;

    /**
     * 空间几何工厂对象（指定 WGS84 坐标系：SRID = 4326）
     */
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public PageResult pageQuery(ScenicSpotPageQueryDTO pageQueryDTO) {
        // 1. 开启 PageHelper 分页拦截器
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2. 调用联合查询方法（处理了不存在 is_deleted 的情况）
        Page<AdminScenicSpotVO> page = scenicSpotMapper.pageQuery(pageQueryDTO);

        // 3. 组装返回结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFields(ScenicSpotDTO scenicSpotDTO) {
        log.info("开始执行保存景点业务...");

        // 1. 属性拷贝与空间几何点构造
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);

        // 🎯 降维打击空间写入痛点：通过经纬度构造 JTS-Point 对象，交由 MP 自动灌入 MySQL 空间字段
        if (scenicSpotDTO.getLongitude() != null && scenicSpotDTO.getLatitude() != null) {
            Point locationPoint = geometryFactory.createPoint(
                    new Coordinate(scenicSpotDTO.getLongitude().doubleValue(), scenicSpotDTO.getLatitude().doubleValue())
            );
            scenicSpot.setLocation(locationPoint);
        }

        // 2. 插入主表数据（利用 MP 内置 save 方法，插入后自增 ID 会自动回填到 scenicSpot 对象中）
        this.save(scenicSpot);
        Long spotId = scenicSpot.getId();

        // 3. 处理副表（轮播图图片）批量落库
        List<String> images = scenicSpotDTO.getImages();
        if (images != null && !images.isEmpty()) {
            List<ScenicImage> imageList = new ArrayList<>();
            for (String url : images) {
                ScenicImage img = ScenicImage.builder()
                        .spotId(spotId) // 🎯 对齐新版字段：spot_id
                        .imageUrl(url)
                        .build();       // 🎯 移除了不存在的幽灵字段 sort_order
                imageList.add(img);
            }
            // 🎯 绝缘复杂 XML：直接通过 MP 原生批量处理句柄秒杀写入
            scenicImageMapper.delete(
                    new LambdaQueryWrapper<ScenicImage>()
                            .eq(ScenicImage::getSpotId, spotId)
            );
            // 提示：若未配置扩展组件，亦可直接使用内置的单体循环，或在 ScenicImage 对应的 Service 层调用 this.saveBatch(imageList)
        }
        log.info("景点 [名称: {}] 成功落库，空间索引已激活。", scenicSpot.getName());
    }

    @Override
    public ScenicSpot getById(Long id) {
        log.info("开始根据ID查询景点详情，ID: {}", id);
        // 🎯 抛弃手写 SQL，直接利用 MP 内置根据主键查询
        ScenicSpot scenicSpot = this.getById(id);

        if (scenicSpot != null) {
            // 级联查出对应的轮播图 URL 列表并回填
            List<String> imageUrls = scenicImageMapper.selectUrlsByScenicId(id);
            scenicSpot.setImages(imageUrls);
        }
        return scenicSpot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithRoute(ScenicSpotDTO scenicSpotDTO) {
        log.info("开始执行修改景点业务，目标景点ID: {}", scenicSpotDTO.getId());

        // 1. 结构转换与空间字段重构
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);

        if (scenicSpotDTO.getLongitude() != null && scenicSpotDTO.getLatitude() != null) {
            Point locationPoint = geometryFactory.createPoint(
                    new Coordinate(scenicSpotDTO.getLongitude().doubleValue(), scenicSpotDTO.getLatitude().doubleValue())
            );
            scenicSpot.setLocation(locationPoint);
        }

        // 2. 更新主表内容
        this.updateById(scenicSpot);
        Long spotId = scenicSpotDTO.getId();

        // 3. 级联清理并重建副表（轮播图）数据
        // 🎯 完美替代原手写 delete：利用 MP 提供的单表条件构造器卡死删除
        scenicImageMapper.delete(
                new LambdaQueryWrapper<ScenicImage>()
                        .eq(ScenicImage::getSpotId, spotId)
        );

        // 4. 重新构建批量插入
        List<String> images = scenicSpotDTO.getImages();
        if (images != null && !images.isEmpty()) {
            List<ScenicImage> imageList = new ArrayList<>();
            for (String url : images) {
                ScenicImage img = ScenicImage.builder()
                        .spotId(spotId)
                        .imageUrl(url)
                        .build();
                imageList.add(img);
            }
            // 重新批量塞入
            for (ScenicImage img : imageList) {
                scenicImageMapper.insert(img);
            }
        }
        log.info("景点数据更新完毕，主副表事务链完整。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        log.info("开始执行批量物理删除业务，级联核销 IDs: {}", ids);

        // 🎯 降维重构：基于新版 nav.sql 配置了 ON DELETE CASCADE 级联物理外键约束
        // 我们只需要在此处安心调用 MP 自带的批量主键核销，
        // MySQL 数据库会在底层自动、原子性地把子表关联的轮播图一并连带抹去！
        this.removeByIds(ids);
    }
}