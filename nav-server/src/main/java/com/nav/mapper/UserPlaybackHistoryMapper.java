package com.nav.mapper;

import com.nav.entity.UserPlaybackHistory;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserPlaybackHistoryMapper {

    /**
     * 根据用户ID和景点ID精准抓取唯一的足迹历史
     * 痛点驱动：得益于 application.yml 中配置的 map-underscore-to-camel-case 开关，
     * 所有的下划线字段会自动无缝流入驼峰属性，彻底告别手工编写 @Results 映射的繁琐！
     */
    @Select("SELECT id, user_id, spot_id, play_count, last_progress, last_triggered_at " +
            "FROM user_playback_history WHERE user_id = #{userId} AND spot_id = #{spotId}")
    UserPlaybackHistory selectByUserIdAndSpotId(@Param("userId") Long userId, @Param("spotId") Long spotId);

    /**
     * 插入新足迹点记录
     */
    @Insert("INSERT INTO user_playback_history (user_id, spot_id, play_count, last_progress, last_triggered_at) " +
            "VALUES (#{userId}, #{spotId}, #{playCount}, #{lastProgress}, #{lastTriggeredAt})")
    void insertHistory(UserPlaybackHistory history);

    /**
     * 动态更新已有进度秒数及播放频次
     */
    @Update("UPDATE user_playback_history " +
            "SET play_count = #{playCount}, last_progress = #{lastProgress}, last_triggered_at = #{lastTriggeredAt} " +
            "WHERE id = #{id}")
    void updateHistory(UserPlaybackHistory history);
}