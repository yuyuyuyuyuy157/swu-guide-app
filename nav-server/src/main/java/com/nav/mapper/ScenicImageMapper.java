package com.nav.mapper;

import com.nav.entity.ScenicImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 🎯 景点轮播图附属表数据访问层
 * 严格对齐苍穹外卖规范：主副表分离查询，提升高并发下的检索效率
 */
@Mapper
public interface ScenicImageMapper {

    /**
     * 1. 用户端：根据景点ID查询关联的轮播图URL列表
     * 痛点驱动：拒绝 SELECT *，只抓取 image_url 单列并按 sort_order 升序，极限压缩内网 I/O 带宽
     * @param scenicId 景点ID
     * @return 图片URL集合
     */
    @Select("SELECT image_url FROM scenic_images WHERE scenic_id = #{scenicId} ORDER BY sort_order ASC")
    List<String> selectUrlsByScenicId(Long scenicId);

    /**
     * 2. 管理端：根据景点ID物理删除所有关联图片
     * 业务场景：当管理员在 B 端编辑景点，重新上传了一批新的轮播图时，我们需要先清理旧图，再插入新图
     * @param scenicId 景点ID
     */
    @Delete("DELETE FROM scenic_images WHERE scenic_id = #{scenicId}")
    void deleteByScenicId(Long scenicId);

    /**
     * 3. 管理端：批量插入新的轮播图记录
     * 痛点驱动：利用 MyBatis 的 <foreach> 动态 SQL，一次网络请求批量写入多张图片，告别 for 循环 insert
     * @param images 图片实体集合
     */
    void insertBatch(@Param("images") List<ScenicImage> images);
}