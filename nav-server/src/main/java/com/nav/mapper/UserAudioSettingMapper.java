package com.nav.mapper;

import com.nav.entity.UserAudioSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAudioSettingMapper {
    /**
     * 插入或更新用户音频设置
     */
    void saveOrUpdate(UserAudioSetting setting);
}