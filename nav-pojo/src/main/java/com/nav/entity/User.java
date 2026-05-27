package com.nav.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.*;
@Data
@TableName("users")
@Builder // 让 Lombok 自动为你生成全套一套精美的 `.builder()` 链式构造链
@NoArgsConstructor  // 自动生成无参构造器（MyBatis-Plus 射流序列化查询时没有它会直接崩溃）
@AllArgsConstructor // @Builder 必须配合全参构造器才能在编译期顺利通过
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

    // 0: 随位置切（默认）  1: 播完再切
    private Integer playMode;

    // 是否开启自动播放（0: 关闭  1: 开启）
    private Integer autoPlay;
}