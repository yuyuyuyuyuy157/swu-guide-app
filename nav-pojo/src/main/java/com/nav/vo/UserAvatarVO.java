package com.nav.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "头像上传成功返回视图对象")
public class UserAvatarVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "新头像URL")
    private String avatarUrl;
}