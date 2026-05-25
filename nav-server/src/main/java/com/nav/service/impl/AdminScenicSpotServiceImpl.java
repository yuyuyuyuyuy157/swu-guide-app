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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class AdminScenicSpotServiceImpl implements AdminScenicSpotService {

    @Autowired
    private AdminScenicSpotMapper adminScenicSpotMapper;

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
    @Transactional // 开启事务控制
    public void saveWithFields(ScenicSpotDTO scenicSpotDTO) {
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);
        scenicSpot.setIsDeleted(0); // 默认未删除

        // 核心：调用具有 @AutoFill 审计注解的 Mapper 方法
        adminScenicSpotMapper.insert(scenicSpot);
    }

    @Override
    public ScenicSpotDTO getById(Long id) {
        ScenicSpot spot = adminScenicSpotMapper.getById(id);
        ScenicSpotDTO dto = new ScenicSpotDTO();
        if (spot != null) {
            BeanUtils.copyProperties(spot, dto);
        }
        return dto;
    }

    @Override
    @Transactional
    public void updateWithFields(ScenicSpotDTO scenicSpotDTO) {
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);

        // 调用具有 @AutoFill 审计注解的修改方法
        adminScenicSpotMapper.update(scenicSpot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 🎯涉及多条数据变更，必须开启事务，保证原子性
    public void deleteBatch(List<Long> ids) {
        log.info(" 开始执行批量软删除业务，级联核销 IDs: {}", ids);

        // 核心优化：直接一行代码，把集合传给 Mapper 依靠动态拼装 SQL 一次性核销
        adminScenicSpotMapper.softDeleteByIds(ids);
    }

}