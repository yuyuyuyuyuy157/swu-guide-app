package com.nav.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CurrentLocationDTO implements Serializable {
    private Double latitude;  // 纬度
    private Double longitude; // 经度
}