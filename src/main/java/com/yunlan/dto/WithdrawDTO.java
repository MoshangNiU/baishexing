package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawDTO {
    private BigDecimal amount;
    private String accountInfo;
}
