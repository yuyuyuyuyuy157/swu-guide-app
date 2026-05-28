package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // 🎯 引入父类
import com.nav.entity.InvitationCode; // 假设你的实体类叫这个
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvitationCodeMapper extends BaseMapper<InvitationCode> {
}