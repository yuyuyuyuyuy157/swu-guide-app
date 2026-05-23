package com.nav.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 统一封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数（前端分页插件需要靠这个计算总页数）
     */
    private Long total;

    /**
     * 当前页的数据集合（通常存放 VO 对象的 List 集合）
     */
    private List records;

}