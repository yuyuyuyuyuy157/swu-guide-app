package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户注册请求参数")
public class UserRegisterDTO {

    @Schema(description = "11位手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "8-16位明文密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^\\S{8,16}$", message = "密码必须为8-16位且不能包含空格")
    private String password;

    @Schema(description = "注册邀请码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邀请码不能为空")
    private String invitationCode;
}