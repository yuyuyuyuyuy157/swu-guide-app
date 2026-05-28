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

@TableName("user_audio_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAudioSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 🎯 核心注意：由于是 1:1 关联用户表，主键就是 userId，不需要自增，策略为 INPUT
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    private Integer autoPlayEnabled;

    private Integer repeatPolicy;

    private Integer switchPolicy;

    private Integer backgroundPlayEnabled;

    private LocalDateTime updatedAt;
}