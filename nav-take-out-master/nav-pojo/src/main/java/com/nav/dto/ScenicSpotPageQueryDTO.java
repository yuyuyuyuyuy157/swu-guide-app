package com.nav.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ScenicSpotPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String keyword;
}
