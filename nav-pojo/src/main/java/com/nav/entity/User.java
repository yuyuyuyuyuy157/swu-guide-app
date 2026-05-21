package com.nav.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.ASSIGN_ID) // 雪花算法全局唯一ID
    private Long id;

    private String phone;
    private String passwordHash;
    private String avatarUrl;
    private String role;
    private Integer status; // 账户状态：1-正常, 2-冻结, 3-注销

    private LocalDateTime passwordLastChangedAt;

    @TableLogic // 🎯 MyBatis-Plus 软删除黄金注解：开启后，delete 会自动变 update
    private Integer isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}