package com.yunlan.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.yunlan.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    /** 验证码有效期：5分钟 */
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000;

    /** 验证码最大长度 */
    private static final int CODE_LENGTH = 6;

    /** 每分钟清理过期码的间隔 */
    private static final long CLEANUP_INTERVAL_SEC = 60;

    /** phone -> { code, timestamp } */
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanupExpired, CLEANUP_INTERVAL_SEC, CLEANUP_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void sendCode(String phone) {
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        codeStore.put(phone, new CodeEntry(code, System.currentTimeMillis()));

        // Dev mode: log code to console so developers can test
        log.info("===== DEV MODE: verification code for {} is: {} =====", phone, code);

        // TODO: In production, integrate with Aliyun SMS / Tencent Cloud SMS here
        // Example:
        // aliyunSmsClient.sendSms(phone, signName, templateCode, "{\"code\":\"" + code + "\"}");
    }

    @Override
    public void verifyCode(String phone, String code) {
        CodeEntry entry = codeStore.get(phone);
        if (entry == null) {
            throw new IllegalArgumentException("验证码未发送或已过期，请重新获取");
        }
        if (System.currentTimeMillis() - entry.timestamp > CODE_EXPIRE_MS) {
            codeStore.remove(phone);
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            throw new IllegalArgumentException("验证码错误");
        }
        // 验证成功后清除
        codeStore.remove(phone);
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        codeStore.values().removeIf(e -> now - e.timestamp > CODE_EXPIRE_MS);
    }

    private static class CodeEntry {
        final String code;
        final long timestamp;

        CodeEntry(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}
