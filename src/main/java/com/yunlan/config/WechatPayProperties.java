package com.yunlan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yunlan.wechat.pay")
public class WechatPayProperties {
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String privateKeyContent;
    private String notifyUrl;
    private String domain;
}
