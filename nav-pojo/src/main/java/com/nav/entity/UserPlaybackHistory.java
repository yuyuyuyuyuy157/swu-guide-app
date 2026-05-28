package com.nav.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 🎯 用户播放足迹实体
 * 严格对应你的 nav.sql 中的 user_playback_history 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlaybackHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;            // 用户 ID
    private Long spotId;            // 景点 ID
    private Integer playCount;      // 累计播放次数
    private Integer lastProgress;   // 🎯 补丁字段：上次播放到的进度断点（秒）
    private LocalDateTime lastTriggeredAt; // 最近一次触发/播放的时间
}