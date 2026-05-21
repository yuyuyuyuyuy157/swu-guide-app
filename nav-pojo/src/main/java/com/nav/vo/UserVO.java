package com.nav.vo;

import  io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
@Schema(description="用户信息展示")
public class UserVO{
    @Schema(description = "用户ID")
    private String userId;

    @Schema(description="手机号(脱敏展示)")
    private String phone;

    @Schema(description="当前头像URL")
    private String avatar;

    @Schema(description = "账户角色")
    private String role;

}