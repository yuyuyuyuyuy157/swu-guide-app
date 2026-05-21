package com.nav.controller;

import com.nav.dto.UserLoginDTO;
import com.nav.dto.UserRegisterDTO;
import com.nav.result.Result;
import com.nav.service.UserService;
import com.nav.vo.UserLoginVO;
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
@RequestMapping("/api/v1/user")
@Tag(name = "用户模块接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户密码登录")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO) {
        log.info("收到用户登录请求，手机号: {}, request_id: {}", userLoginDTO.getPhone(), userLoginDTO.getRequestId());

        // 调用业务层执行登录
        UserLoginVO userLoginVO = userService.login(userLoginDTO);

        return Result.success(userLoginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户邀请码注册")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("收到新用户注册请求，手机号: {}, 使用邀请码: {}", userRegisterDTO.getPhone(), userRegisterDTO.getInvitationCode());

        userService.register(userRegisterDTO);

        return Result.success("注册成功");
    }

    @GetMappping("/info/{userId}")
    @Operation(summary="获取当前登录用户信息")
    public Result<UserVO> getCurrentInfo(@PathVariable Long userId){
        log.info("获取用户信息，用户ID: {}",userId);
        UserVo userVo=userService.getCurrentInfo(userId);
        return Result.success(userVO);

    }
}