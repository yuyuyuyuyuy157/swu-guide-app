package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户密码登录请求参数")
public class UserLoginDTO {

    @Schema(description = "11位中国大陆手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "8-16位密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "客户端生成的唯一UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "request_id 不能为空")
    private String requestId;
}