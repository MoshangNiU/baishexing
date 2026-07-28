package com.yunlan.service;

/**
 * 微信小程序服务
 */
public interface WechatService {
    /**
     * 通过临时code获取微信openid
     * @param code 微信登录临时code（通过uni.login获取）
     * @return openid
     * @throws IllegalArgumentException code无效或微信API调用失败
     */
    String getOpenid(String code);
}
