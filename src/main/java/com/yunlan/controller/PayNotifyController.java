package com.yunlan.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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

    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        try {
            String body = new BufferedReader(request.getReader())
                    .lines().collect(Collectors.joining("\n"));
            JSONObject json = JSONUtil.parseObj(body);

            String eventType = json.getStr("event_type");
            if ("TRANSACTION.SUCCESS".equals(eventType)) {
                JSONObject resource = json.getJSONObject("resource");
                if (resource != null) {
                    String ciphertext = resource.getStr("ciphertext");
                    JSONObject decrypt = JSONUtil.parseObj(ciphertext);
                    String outTradeNo = decrypt.getStr("out_trade_no");
                    String transactionId = decrypt.getStr("transaction_id");

                    // 从tradingOrderNo反查orderId并处理
                    ordersService.handlePayNotify(outTradeNo, transactionId);
                }
            }
            return JSONUtil.createObj()
                    .set("code", "SUCCESS")
                    .set("message", "OK")
                    .toStringPretty();
        } catch (Exception e) {
            log.error("Pay notify error", e);
            return JSONUtil.createObj()
                    .set("code", "FAIL")
                    .set("message", e.getMessage())
                    .toStringPretty();
        }
    }
}
