package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // 🎯 引入父类
import com.nav.entity.UserPlaybackHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPlaybackHistoryMapper extends BaseMapper<UserPlaybackHistory> {
    // 彻底干掉手工编写的 @Select、@Insert、@Update
    // 通用 CRUD 能力已上交由 MyBatis-Plus 托管
}