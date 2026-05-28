package com.nav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("invitation_codes") // 🎯 显式指定映射的数据库表名
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO) // 🎯 指定主键自增策略
    private Long id;

    /**
     * 唯一邀请码文本
     */
    private String code;

    /**
     * 使用状态：1-未使用，2-已使用，3-已失效
     */
    private Integer status;

    /**
     * 使用该邀请码的用户ID
     */
    private Long usedByUserId; // 🎯 自动映射数据库下划线字段 used_by_user_id

    /**
     * 使用时间
     */
    private LocalDateTime usedAt; // 🎯 自动映射数据库下划线字段 used_at

    /**
     * 过期时间（NULL表示永不过期）
     */
    private LocalDateTime expireAt; // 🎯 自动映射数据库下划线字段 expire_at

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    private Long createdBy;
}