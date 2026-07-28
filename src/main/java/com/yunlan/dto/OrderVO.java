package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private Long serveItemId;
    private Integer ordersStatus;
    private Integer payStatus;
    private String serveItemImg;
    private String serveItemName;
    private String serveTypeName;
    private Integer purNum;
    private String unit;
    private BigDecimal price;
    private BigDecimal realPayAmount;
    private String serveAddress;
    private String contactsName;
    private String contactsPhone;
    private String serverName;
    private LocalDateTime serveStartTime;
    private LocalDateTime serveActualEndTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String createTime;
}
