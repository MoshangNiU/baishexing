package com.yunlan.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yunlan.service.WechatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Service
public class WechatServiceImpl implements WechatService {

    private static final Logger log = LoggerFactory.getLogger(WechatServiceImpl.class);
    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Value("${yunlan.wechat.appid}")
    private String appid;

    @Value("${yunlan.wechat.secret}")
    private String secret;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public String getOpenid(String code) {
        String url = JSCODE2SESSION_URL + "?appid=" + appid
                + "&secret=" + secret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        String response;
        try {
            response = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("微信 jscode2session 调用失败: {}", e.getMessage());
            throw new IllegalArgumentException("微信登录服务暂不可用，请稍后再试");
        }

        if (response == null) {
            throw new IllegalArgumentException("微信登录失败：返回为空");
        }

        JSONObject json = JSONUtil.parseObj(response);

        // Check for WeChat API error
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "未知错误");
            log.error("微信 jscode2session 返回错误: code={}, msg={}", errcode, errmsg);
            throw new IllegalArgumentException("微信登录失败：" + errmsg);
        }

        String openid = json.getStr("openid");
        if (openid == null) {
            throw new IllegalArgumentException("微信登录失败：未获取到openid");
        }

        log.info("微信登录成功，openid={}", openid);
        return openid;
    }
}
