package com.nav.controller;

import com.nav.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "系统通用模块接口")
public class SystemController {

    @Value("${nav.system.default-latitude:29.815}")
    private Double defaultLatitude;

    @Value("${nav.system.default-longitude:106.425}")
    private Double defaultLongitude;

    @Value("${nav.system.default-scenic-id:1}")
    private String defaultScenicId;

    @Value("${nav.system.user-agreement-url:https://swu-guide-app.com/agreement}")
    private String userAgreementUrl;

    @Value("${nav.system.privacy-policy-url:https://swu-guide-app.com/privacy}")
    private String privacyPolicyUrl;

    @GetMapping("/config")
    @Operation(summary = "获取APP全局配置")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("defaultLatitude", defaultLatitude);
        config.put("defaultLongitude", defaultLongitude);
        config.put("defaultScenicId", defaultScenicId);
        config.put("userAgreementUrl", userAgreementUrl);
        config.put("privacyPolicyUrl", privacyPolicyUrl);
        return Result.success(config);
    }
}
