package com.nav.mapper;

import com.github.pagehelper.Page;
import com.nav.annotation.AutoFill;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.entity.ScenicSpot;
import com.nav.enumeration.OperationType;
import com.nav.vo.AdminScenicSpotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminScenicSpotMapper {

    /**
     * 管理端联合查询（左连接管理员表，查出修改人名字）
     */
    Page<AdminScenicSpotVO> pageQuery(ScenicSpotPageQueryDTO pageQueryDTO);

    /**
     * 自动审计注入：新增景点
     */
    @AutoFill(OperationType.INSERT)
    void insert(ScenicSpot scenicSpot);

    /**
     * 自动审计注入：修改景点
     */
    @AutoFill(OperationType.UPDATE)
    void update(ScenicSpot scenicSpot);

    @Select("select id, name, description, image_url, audio_url, latitude, longitude, radius from scenic_spots where id = #{id} and is_deleted = 0")
    ScenicSpot getById(Long id);

    /**
     * 软删除核心语句
     */
    void softDeleteByIds(@Param("ids") List<Long> ids);


}