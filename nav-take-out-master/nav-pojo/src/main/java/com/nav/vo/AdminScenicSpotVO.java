package com.nav.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminScenicSpotVO implements Serializable {
    private String scenicId;       // 景点ID
    private String name;           // 景点名称
    private String intro;          // 景点简介片段
    private String lastModifier;   // 上次修改人姓名
    private LocalDateTime modifyTime; // 上次修改时间
}