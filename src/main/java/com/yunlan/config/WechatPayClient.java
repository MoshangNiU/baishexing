package com.yunlan.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class WechatPayClient {

    private static final Logger log = LoggerFactory.getLogger(WechatPayClient.class);
    private static final String BASE_URL = "https://api.mch.weixin.qq.com";

    @Resource
    private WechatPayProperties properties;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            String keyContent = properties.getPrivateKeyContent();
            if (keyContent == null || keyContent.isEmpty()) {
                String path = properties.getPrivateKeyPath();
                if (path != null && !path.isEmpty()) {
                    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                    if (is == null) {
                        is = getClass().getResourceAsStream(path);
                    }
                    if (is != null) {
                        keyContent = new BufferedReader(new InputStreamReader(is))
                                .lines().collect(Collectors.joining("\n"));
                    }
                }
            }
            if (keyContent != null && !keyContent.isEmpty()) {
                String pem = keyContent
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                byte[] encoded = Base64.getDecoder().decode(pem);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                privateKey = kf.generatePrivate(keySpec);
            }
        } catch (Exception e) {
            log.error("Failed to load WeChat Pay private key", e);
        }
    }

    private String buildSign(String method, String urlPath, String body, String nonce, long timestamp) {
        String message = method + "\n" + urlPath + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (Exception e) {
            throw new RuntimeException("WeChat Pay sign failed", e);
        }
    }

    public JSONObject doPost(String urlPath, JSONObject body) {
        try {
            String bodyStr = body.toStringPretty();
            String nonce = IdUtil.fastSimpleUUID();
            long timestamp = System.currentTimeMillis() / 1000;
            String sign = buildSign("POST", urlPath, bodyStr, nonce, timestamp);
            String token = "WECHATPAY2-SHA256-RSA2048 "
                    + "mchid=\"" + properties.getMchId() + "\","
                    + "nonce_str=\"" + nonce + "\","
                    + "timestamp=\"" + timestamp + "\","
                    + "serial_no=\"" + getSerialNo() + "\","
                    + "signature=\"" + sign + "\"";

            URL url = new URL(BASE_URL + urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "yunlan-backend");

            conn.getOutputStream().write(bodyStr.getBytes(StandardCharsets.UTF_8));

            int code = conn.getResponseCode();
            String responseBody;
            if (code >= 200 && code < 300) {
                try (InputStream is = conn.getInputStream()) {
                    responseBody = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                }
            } else {
                try (InputStream es = conn.getErrorStream()) {
                    responseBody = es != null
                            ? new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"))
                            : "";
                }
            }

            JSONObject result = JSONUtil.parseObj(responseBody);
            result.set("_status_code", code);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("WeChat Pay API call failed: " + urlPath, e);
        }
    }

    private String serialNo;

    private String getSerialNo() {
        return serialNo != null ? serialNo : "loading";
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
}
