package com.nav.vo;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

/**
 * 景点语音详情视图对象
 */
@Data
@Builder
public class AudioDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String audioUrl;      // 语音播放地址
    private Integer duration;     // 语音总时长（秒）
    private String title;         // 语音标题（通常为景点名称）
    private Integer lastProgress; // 上次播放进度（暂默认为0，后续可根据用户听书记录扩展）
}