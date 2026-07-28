package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer couponCount;
    private Integer evaluationCount;
    private Integer orderCount;
    private String inviteCode;
    private BigDecimal balance;
    private BigDecimal totalRebate;
}
