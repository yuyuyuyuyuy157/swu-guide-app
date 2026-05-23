package com.nav.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;                  // 主键ID

    private String username;          // 用户名（登录账号）

    private String name;              // 姓名

    private String password;          // 密码（通常在数据库存MD5或BCrypt加密后的密文）

    private String phone;             // 手机号

    private String sex;               // 性别

    private String idNumber;          // 身份证号

    private Integer status;           // 账号状态：0-禁用，1-启用

    // ------------ 以下为企业级项目必备的公共审计字段 ------------

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 修改时间

    private Long createUser;          // 创建人ID

    private Long updateUser;          // 修改人ID
}