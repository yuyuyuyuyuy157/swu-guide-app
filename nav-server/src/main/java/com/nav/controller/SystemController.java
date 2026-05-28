package com.nav.controller;

import com.nav.properties.SystemConfigProperties;
import com.nav.result.Result;
import com.nav.vo.SystemConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 🎯 系统通用模块控制器
 * 严格对齐《接口文档5.18.1》4.1 节契约
 */
@RestController
@RequestMapping("/api/v1/system")
@Slf4j
@Tag(name = "系统通用模块接口")
public class SystemController {

    @Autowired
    private SystemConfigProperties systemConfigProperties;

    /**
     * 4.1 获取APP全局配置
     * 接口路径：GET /api/v1/system/config
     * 权限要求：无需登录（游客及冷启动状态可用）
     */
    @GetMapping("/config")
    @Operation(summary = "获取APP全局配置")
    public Result<SystemConfigVO> getSystemConfig() {
        log.info("📡 APP触发冷启动流，开始装配全局配置载荷...");

        SystemConfigVO configVO = SystemConfigVO.builder()
                .defaultLatitude(systemConfigProperties.getDefaultLatitude())
                .defaultLongitude(systemConfigProperties.getDefaultLongitude())
                .defaultScenicId(systemConfigProperties.getDefaultScenicId())
                .userAgreementUrl(systemConfigProperties.getUserAgreementUrl())
                .privacyPolicyUrl(systemConfigProperties.getPrivacyPolicyUrl())
                .build();

        return Result.success(configVO);
    }
}