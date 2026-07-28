package com.yunlan.dto;

import lombok.Data;

@Data
public class PayOrderDTO {
    private Integer payChannel;
    private Long tradingOrderNo;
    private Long couponId;
}
