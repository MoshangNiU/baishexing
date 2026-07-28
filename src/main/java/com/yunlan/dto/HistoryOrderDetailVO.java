package com.yunlan.dto;

import lombok.Data;

@Data
public class HistoryOrderDetailVO {
    private OrderVO orderInfo;
    private OrderVO refundInfo;
    private String serverName;
    private Integer ordersStatus;
    private Integer payStatus;
}
