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

    /**
     * 商户API证书序列号
     * 用于请求微信支付API时的 Authorization 头
     * 获取方式：登录微信支付商户平台 → 账户中心 → API安全 → 查看证书
     */
    private String mchSerialNo;
}
