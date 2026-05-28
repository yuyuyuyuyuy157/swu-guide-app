package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nav.entity.UserAudioSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAudioSettingMapper extends BaseMapper<UserAudioSetting> {
    // 彻底干掉自定义的 saveOrUpdate XML 语句，这里什么都不用写！
}