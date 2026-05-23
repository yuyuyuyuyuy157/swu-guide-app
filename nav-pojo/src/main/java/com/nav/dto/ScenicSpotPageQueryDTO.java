package com.nav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpotPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    //搜索关键词（支持匹配景点名称、简介等）
    private String keyword;


    //页码（从 1 开始）
    private Integer page;

    //每页展示的条数
    private Integer pageSize;

}