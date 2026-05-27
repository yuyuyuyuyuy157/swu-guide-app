package com.nav.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "baidu.map")
@Data
public class BaiduMapProperties {
    private String ak; // 自动读取 yml 中的 baidu.map.ak
}