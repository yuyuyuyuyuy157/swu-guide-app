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
@Schema(description = "用户登录返回数据")
public class UserLoginVO {

    @Schema(description = "登录凭证，有效期7天")
    private String token;

    @Schema(description = "用户唯一ID")
    private String userId;

    @Schema(description = "用户手机号（中间4位打码）")
    private String phone;

    @Schema(description = "用户头像 URL")
    private String avatar;

    @Schema(description = "用户角色：user普通用户，admin管理员")
    private String role;

    @Schema(description = "用户最新的播放设置")
    private Object playSettings;
}