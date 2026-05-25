package com.nav.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户修改密码请求参数")
public class UserEditPasswordDTO {

    @Schema(description = "原明文密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新明文密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^\\S{8,16}$", message = "新密码必须为8-16位且不能包含空格")
    private String newPassword;

    @Schema(description = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "newpass123")
    @NotBlank(message = "确认新密码不能为空")
    @Pattern(regexp = "^\\S{8,16}$", message = "确认新密码必须为8-16位且不能包含空格")
    private String confirmNewPassword;

    @Schema(description = "客户端生成的唯一UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "request_id 不能为空")
    private String requestId;
}

