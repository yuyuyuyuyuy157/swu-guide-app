package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.nav.annotation.AutoFill;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.entity.ScenicSpot;
import com.nav.enumeration.OperationType;
import com.nav.vo.AdminScenicSpotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminScenicSpotMapper extends BaseMapper<ScenicSpot> {

    /**
     * 1. 管理端联合查询（左连接管理员表，查出修改人名字）
     */
    Page<AdminScenicSpotVO> pageQuery(ScenicSpotPageQueryDTO pageQueryDTO);

    /**
     * 2. 批量软删除
     */
    void softDeleteByIds(@Param("ids") List<Long> ids);


    /**
     * 自动审计注入：新增景点（重写父类 insert，确保 @AutoFill 拦截器正常生效）
     */
    @Override
    @AutoFill(OperationType.INSERT)
    int insert(ScenicSpot scenicSpot);

    /**
     * 自动审计注入：修改景点（重写父类 updateById，确保 @AutoFill 拦截器正常生效）
     */
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param("et") ScenicSpot scenicSpot);
}