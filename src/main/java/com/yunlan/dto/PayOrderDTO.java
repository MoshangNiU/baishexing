package com.yunlan.dto;

import lombok.Data;

@Data
public class PayOrderDTO {
    private Integer payChannel;
    private Long tradingOrderNo;
    private Long couponId;
    private String openId;
    private String tradingChannel;
    private String clientIp;
}
