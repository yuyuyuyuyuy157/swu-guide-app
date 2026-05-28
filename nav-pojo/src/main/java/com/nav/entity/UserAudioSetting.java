package com.nav.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAudioSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;                  // 唯一约束
    private Integer autoPlayEnabled;      // 0-关, 1-开
    private Integer repeatPolicy;
    private Integer switchPolicy;
    private Integer backgroundPlayEnabled;// 0-关, 1-开
    private Float defaultSpeed;
    private Integer backwardForwardDuration;
    private LocalDateTime updatedAt;
}