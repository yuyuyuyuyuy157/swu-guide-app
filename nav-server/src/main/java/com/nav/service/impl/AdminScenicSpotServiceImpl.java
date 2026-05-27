package com.nav.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nav.dto.ScenicSpotDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.entity.ScenicSpot;
import com.nav.mapper.AdminScenicSpotMapper;
import com.nav.result.PageResult;
import com.nav.service.AdminScenicSpotService;
import com.nav.vo.AdminScenicSpotVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.nav.entity.ScenicImage; // 引入实体
import com.nav.mapper.ScenicImageMapper; // 引入图片Mapper
@Slf4j
@Service
public class AdminScenicSpotServiceImpl implements AdminScenicSpotService {

    @Autowired
    private AdminScenicSpotMapper adminScenicSpotMapper;
    @Autowired
    private ScenicImageMapper scenicImageMapper;
    @Override
    public PageResult pageQuery(ScenicSpotPageQueryDTO pageQueryDTO) {
        // 1. 开启 PageHelper 分页拦截器（必须在执行 SQL 的上一行调用）
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2. 调用管理端专用的持久层联合查询方法
        Page<AdminScenicSpotVO> page = adminScenicSpotMapper.pageQuery(pageQueryDTO);

        // 3. 组装返回，包含 total（总条数）和 result（清洗后的联合查询 VO 列表）
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 🎯 空间地理操作涉及多级校验，开启声明式事务控制
    @com.nav.annotation.AutoFill(value = com.nav.enumeration.OperationType.INSERT) // 🎯 挂载苍穹核心审计切面
    public void saveWithFields(ScenicSpotDTO scenicSpotDTO) {
        log.info("开始执行保存景点业务，表单清洗中...");
        // 1. 保存主表
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);
        scenicSpot.setIsDeleted(0);
        adminScenicSpotMapper.insert(scenicSpot);

        // 2. 保存副表（轮播图）
        Long scenicId = scenicSpot.getId();
        List<String> images = scenicSpotDTO.getImages();

        if (images != null && !images.isEmpty()) {
            List<ScenicImage> imageList = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                ScenicImage img = new ScenicImage();
                img.setScenicId(scenicId);
                img.setImageUrl(images.get(i));
                img.setSortOrder(i + 1); // 排序号按数组顺序 1, 2, 3...
                imageList.add(img);
            }
            // 批量插入
            scenicImageMapper.insertBatch(imageList);
        }
        log.info("景点 [名称: {}] 成功落库，空间位置索引与审计轨迹已全部激活。", scenicSpot.getName());
    }

    @Override
    public ScenicSpot getById(Long id) {
        log.info("开始执行根据ID查询景点详情，ID: {}", id);
        // 调用持久层，查询未被软删除的有效景点
        return adminScenicSpotMapper.getById(id);
    }
    // 修改景点核心内容及关联路径锚点
    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及数据变更，必须开启声明式事务
    @com.nav.annotation.AutoFill(value = com.nav.enumeration.OperationType.UPDATE) // 🎯 触发苍穹公共字段自动填充切面
    public void updateWithRoute(ScenicSpotDTO scenicSpotDTO) {
        log.info("开始执行修改景点业务，目标景点ID: {}", scenicSpotDTO.getId());

        // 1. 结构转换：将 DTO 拷贝至 Entity
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);


        // 2. 执行持久层动态 SQL 更新
        adminScenicSpotMapper.update(scenicSpot);
        // 修改副表（轮播图）
        Long scenicId = scenicSpotDTO.getId();
        List<String> images = scenicSpotDTO.getImages();

        // 无论前端有没有传新图片，只要是修改操作，企业级做法都是：先清空该景点历史的所有轮播图
        scenicImageMapper.deleteByScenicId(scenicId);

        // 如果前端传了新图片，再重新批量插入
        if (images != null && !images.isEmpty()) {
            List<ScenicImage> imageList = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                ScenicImage img = new ScenicImage();
                img.setScenicId(scenicId);
                img.setImageUrl(images.get(i));
                img.setSortOrder(i + 1);
                imageList.add(img);
            }
            scenicImageMapper.insertBatch(imageList);
        }
        log.info("景点主表数据更新完毕，审计信息已自动填充。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及多条数据变更，必须开启事务，保证原子性
    public void deleteBatch(List<Long> ids) {
        log.info(" 开始执行批量软删除业务，级联核销 IDs: {}", ids);

        // 核心优化：直接一行代码，把集合传给 Mapper 依靠动态拼装 SQL 一次性核销
        adminScenicSpotMapper.softDeleteByIds(ids);
    }

}