package com.nav.controller;

import com.nav.dto.AdminLoginDTO;
import com.nav.result.Result;
import com.nav.service.UserService;
import com.nav.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "B端管理员工作台接口")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "管理员账号密码登录")
    public Result<AdminLoginVO> login(@Validated @RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("B端管理工作台收到登录请求，账号: {}", adminLoginDTO.getUsername());
        AdminLoginVO adminLoginVO = userService.adminLogin(adminLoginDTO);
        return Result.success(adminLoginVO);
    }
}