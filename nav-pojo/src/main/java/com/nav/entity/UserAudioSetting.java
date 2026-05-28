package com.nav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("user_audio_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAudioSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    // 自动播放总开关
    private Integer autoPlayEnabled;
    // 频次控制策略（重听策略） 1-每个景点只播放一次（触发后去重控制）；2-可以重复播放多次。
    private Integer repeatPolicy;
    // 触发与切换策略 1-播完再切；2-随位置实时强切；3-增加弹窗提醒。
    private Integer switchPolicy;
    // 后台播放设置开关
    private Integer backgroundPlayEnabled;
    // 配置最后更新时间
    private LocalDateTime updatedAt;
    // 默认讲解播放倍速偏好
    private BigDecimal defaultSpeed;
    // 快进/快退时长（秒）
    private Integer backwardForwardDuration;
}