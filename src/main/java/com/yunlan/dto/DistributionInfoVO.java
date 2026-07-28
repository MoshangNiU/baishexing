package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DistributionInfoVO {
    private String inviteCode;
    private BigDecimal balance;
    private BigDecimal totalRebate;
    private Integer teamCount;
}
