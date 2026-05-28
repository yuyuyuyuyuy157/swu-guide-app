package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "APP全局配置返回视图对象")
public class SystemConfigVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "默认位置纬度")
    private Double defaultLatitude;

    @Schema(description = "默认位置经度")
    private Double defaultLongitude;

    @Schema(description = "默认景点ID")
    private String defaultScenicId;

    @Schema(description = "用户协议地址")
    private String userAgreementUrl;

    @Schema(description = "隐私政策地址")
    private String privacyPolicyUrl;
}