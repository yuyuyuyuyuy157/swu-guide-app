package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "管理员登录请求参数")
public class AdminLoginDTO {

    @Schema(description = "管理员账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "管理员账号不能为空")
    private String username;

    @Schema(description = "管理员密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "管理员密码不能为空")
    private String password;

    @Schema(description = "客户端生成的唯一UUID v4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "request_id 不能为空")
    private String requestId;
}