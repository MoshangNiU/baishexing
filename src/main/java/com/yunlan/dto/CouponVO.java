package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponVO {
    private Long id;
    private Long activityId;
    private String name;
    private BigDecimal discountAmount;
    private BigDecimal conditionAmount;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime endTime;
}
