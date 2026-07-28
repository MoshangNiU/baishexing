package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RebateRecordVO {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
    private String serveItemName;
}
