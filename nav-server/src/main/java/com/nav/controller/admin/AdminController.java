package com.nav.controller.admin;

import com.nav.dto.AdminLoginDTO;
import com.nav.result.Result;
import com.nav.service.UserService;
import com.nav.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 管理员认证核心控制层
 * */
@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
@Tag(name = "管理端-管理员认证接口")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 5.1 管理员登录接口 */
    @PostMapping("/login") // 严格对齐接口文档路径
    @Operation(summary = "管理员密码登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("收到管理端管理员登录请求，用户名/手机号: {}", adminLoginDTO.getUsername());
        AdminLoginVO adminLoginVO = userService.adminLogin(adminLoginDTO);
        log.info("管理员登录成功，视图凭证组装完毕，准备下发。");
        return Result.success(adminLoginVO);
    }
}