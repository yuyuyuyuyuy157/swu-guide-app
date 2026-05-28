package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "当前地理位置数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentLocationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "纬度坐标", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "纬度坐标不能为空") // Double类型使用@NotNull进行非空校验
    private Double latitude;

    @Schema(description = "经度坐标", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经度坐标不能为空") // Double类型使用@NotNull进行非空校验
    private Double longitude;
}