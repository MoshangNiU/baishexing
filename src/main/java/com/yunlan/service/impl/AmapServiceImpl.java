package com.yunlan.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yunlan.service.AmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class AmapServiceImpl implements AmapService {

    private static final Logger log = LoggerFactory.getLogger(AmapServiceImpl.class);
    private static final String AMAP_REGEO_URL = "https://restapi.amap.com/v3/geocode/regeo";

    @Value("${amap.key:}")
    private String amapKey;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public Map<String, Object> reverseGeocode(String lat, String lng) {
        if (amapKey == null || amapKey.isEmpty() || amapKey.contains("你的")) {
            log.warn("AMap key not configured — skipping reverse geocode");
            return null;
        }

        try {
            String url = AMAP_REGEO_URL + "?output=json&location=" + lng + "," + lat + "&key=" + amapKey;
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) return null;

            JSONObject json = JSONUtil.parseObj(response);
            if (!"1".equals(json.getStr("status"))) {
                log.warn("AMap reverse geocode failed: {}", response);
                return null;
            }

            JSONObject regeocode = json.getJSONObject("regeocode");
            if (regeocode == null) return null;

            JSONObject addrComp = regeocode.getJSONObject("addressComponent");
            if (addrComp == null) return null;

            Map<String, Object> result = new HashMap<>();
            result.put("province", addrComp.getStr("province", ""));
            result.put("city", addrComp.getStr("city", ""));
            result.put("district", addrComp.getStr("district", ""));
            result.put("street", addrComp.getStr("street", ""));
            result.put("streetNumber", addrComp.getStr("streetNumber", ""));
            return result;
        } catch (Exception e) {
            log.error("AMap reverse geocode error: {}", e.getMessage());
            return null;
        }
    }
}
