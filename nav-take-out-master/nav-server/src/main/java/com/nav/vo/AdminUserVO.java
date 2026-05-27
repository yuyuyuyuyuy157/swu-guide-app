package com.nav.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminUserVO implements Serializable {
    private String userId;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
}
