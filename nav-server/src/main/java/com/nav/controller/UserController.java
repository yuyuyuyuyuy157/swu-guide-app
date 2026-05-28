package com.nav.controller;

import com.nav.context.BaseContext;
import com.nav.dto.UserLoginDTO;
import com.nav.dto.UserRegisterDTO;
import com.nav.result.Result;
import com.nav.service.UserService;
import com.nav.vo.UserAudioSettingVO;
import com.nav.vo.UserLoginVO;
import com.nav.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.nav.dto.UserEditPasswordDTO;
import com.nav.dto.UserAudioSettingDTO;
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
        log.info("用户登录请求，手机号: {}, request_id: {}", userLoginDTO.getPhone(), userLoginDTO.getRequestId());
        return Result.success(userService.login(userLoginDTO));
    }

    @PostMapping("/register")
    @Operation(summary = "用户邀请码注册")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("新用户注册请求，手机号: {}, 邀请码: {}", userRegisterDTO.getPhone(), userRegisterDTO.getInvitationCode());
        userService.register(userRegisterDTO);
        return Result.success("注册成功");
    }

    @GetMapping("/info/{userId}")
    @Operation(summary = "获取当前登录用户信息")
    public Result<UserVO> getCurrentInfo(@PathVariable Long userId) {
        log.info("获取用户信息，用户ID: {}", userId);
        return Result.success(userService.getCurrentInfo(userId));
    }

    @PostMapping("/change-password") // 路径与接口文档严格对齐：/api/v1/user/change-password [cite: 49]
    public Result<Void> changePassword(@RequestBody UserEditPasswordDTO userEditPasswordDTO) {
        log.info("用户触发修改密码业务，请求ID: {}", userEditPasswordDTO.getRequestId());

        // 1. 基础边界校验
        if (userEditPasswordDTO.getOldPassword() == null || userEditPasswordDTO.getNewPassword() == null) {
            return Result.error(400,"原密码或新密码不能为空");
        }
        if (!userEditPasswordDTO.getNewPassword().equals(userEditPasswordDTO.getConfirmNewPassword())) {
            return Result.error(400,"两次输入的新密码不一致"); // 满足文档“必须与new_password一致”的约束
        }

        // 2. 调用业务层
        userService.changePassword(userEditPasswordDTO);
        return Result.success();
    }

    @PutMapping("/setting/audio")
    @Operation(summary = "保存用户音频播放设置")
    public Result<String> saveAudioSetting(@RequestBody UserAudioSettingDTO userAudioSettingDTO) {
        // 从 ThreadLocal 中安全捞取当前登录用户 ID
        Long userId = BaseContext.getCurrentId();
        log.info("📡 收到保存音频设置请求，用户ID: {}, 载荷: {}", userId, userAudioSettingDTO);

        // 参数校验
        if (userAudioSettingDTO.getPlayMode() == null || userAudioSettingDTO.getAutoPlay() == null) {
            return Result.error(400, "参数校验失败：配置项不能为空");
        }

        userService.updateAudioSetting(userId, userAudioSettingDTO);
        return Result.success("设置保存成功");
    }

    /**
     * 🎯 获取用户音频播放设置
     * 接口路径：GET /api/v1/user/setting/audio
     */
    @GetMapping("/setting/audio")
    @Operation(summary = "获取用户音频播放设置")
    public Result<UserAudioSettingVO> getAudioSetting() {
        Long userId = BaseContext.getCurrentId();
        log.info("📡 收到获取音频设置请求，用户ID: {}", userId);

        UserAudioSettingVO userAudioSettingVO = userService.getAudioSetting(userId);
        return Result.success(userAudioSettingVO);
    }

}
