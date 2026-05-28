package com.nav.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nav.system")
@Data
public class SystemConfigProperties {
    private Double defaultLatitude;
    private Double defaultLongitude;
    private String defaultScenicId;
    private String userAgreementUrl;
    private String privacyPolicyUrl;
}