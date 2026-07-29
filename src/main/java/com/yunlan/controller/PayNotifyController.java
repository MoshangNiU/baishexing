package com.yunlan.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yunlan.config.WechatPayDecryptor;
import com.yunlan.config.WechatPayProperties;
import com.yunlan.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pay")
public class PayNotifyController {

    private static final Logger log = LoggerFactory.getLogger(PayNotifyController.class);

    @Resource
    private OrdersService ordersService;

    @Resource
    private WechatPayProperties wechatPayProperties;

    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        try {
            String body = new BufferedReader(request.getReader())
                    .lines().collect(Collectors.joining("\n"));
            JSONObject json = JSONUtil.parseObj(body);

            String eventType = json.getStr("event_type");
            if (!"TRANSACTION.SUCCESS".equals(eventType)) {
                // 非支付成功通知（如退款通知），直接返回成功避免微信重试
                log.info("Received non-success notify event_type={}", eventType);
                return successResponse();
            }

            JSONObject resource = json.getJSONObject("resource");
            if (resource == null) {
                log.error("Pay notify missing resource field: {}", body);
                return failResponse("missing resource");
            }

            // 微信支付回调密文参数
            String ciphertext = resource.getStr("ciphertext");
            String nonce = resource.getStr("nonce");
            String associatedData = resource.getStr("associated_data");

            // 使用 APIv3Key 做 AES-256-GCM 解密
            String apiV3Key = wechatPayProperties.getApiV3Key();
            if (apiV3Key == null || apiV3Key.isEmpty()) {
                throw new RuntimeException("APIv3Key not configured (yunlan.wechat.pay.api-v3-key)");
            }

            String plainText = WechatPayDecryptor.decrypt(apiV3Key, associatedData, nonce, ciphertext);
            JSONObject decrypt = JSONUtil.parseObj(plainText);

            String outTradeNo = decrypt.getStr("out_trade_no");
            String transactionId = decrypt.getStr("transaction_id");
            String tradeState = decrypt.getStr("trade_state");

            log.info("Pay notify decrypted: outTradeNo={}, transactionId={}, tradeState={}",
                    outTradeNo, transactionId, tradeState);

            // 仅在支付成功时处理订单
            if ("SUCCESS".equals(tradeState)) {
                ordersService.handlePayNotify(outTradeNo, transactionId);
            } else {
                log.warn("Pay notify trade_state is not SUCCESS: {}", tradeState);
            }

            return successResponse();
        } catch (Exception e) {
            log.error("Pay notify error", e);
            return failResponse(e.getMessage());
        }
    }

    private String successResponse() {
        return JSONUtil.createObj()
                .set("code", "SUCCESS")
                .set("message", "OK")
                .toStringPretty();
    }

    private String failResponse(String message) {
        return JSONUtil.createObj()
                .set("code", "FAIL")
                .set("message", message)
                .toStringPretty();
    }
}
