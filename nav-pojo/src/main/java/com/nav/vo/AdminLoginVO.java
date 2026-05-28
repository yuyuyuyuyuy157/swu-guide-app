package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员登录返回数据")
public class AdminLoginVO {

    @Schema(description = "管理员登录凭证，有效期24小时")
    private String token;

    @Schema(description = "管理员唯一ID")
    private String adminId;

    @Schema(description = "管理员姓名")
    private String name;
}