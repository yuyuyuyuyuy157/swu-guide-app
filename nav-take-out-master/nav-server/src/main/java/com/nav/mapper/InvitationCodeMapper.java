package com.nav.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface InvitationCodeMapper {

    /**
     * 🛠️ 纯手工定制：根据邀请码文本查询有效状态的记录
     * git 痛点驱动：由于 invitation_codes 表没有直接用 MP 实体，我们直接手写动态比对最稳健
     */
    @Update("UPDATE invitation_codes SET status = 2, used_by_user_id = #{userId}, used_at = NOW() " +
            "WHERE code = #{code} AND status = 1 AND (expire_at IS NULL OR expire_at > NOW())")
    int useCode(@Param("code") String code, @Param("userId") Long userId);
}