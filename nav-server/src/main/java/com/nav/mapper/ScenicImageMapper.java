package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // 🎯 引入 MyBatis-Plus 父类
import com.nav.entity.ScenicImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 🎯 景点轮播图附属表数据访问层
 */
@Mapper
public interface ScenicImageMapper extends BaseMapper<ScenicImage> { // 🎯 继承 BaseMapper

    /**
     * 1. 用户端：根据景点ID查询关联的轮播图URL列表
     */
    @Select("SELECT image_url FROM scenic_images WHERE spot_id = #{spotId}")
    List<String> selectUrlsByScenicId(Long spotId);

}