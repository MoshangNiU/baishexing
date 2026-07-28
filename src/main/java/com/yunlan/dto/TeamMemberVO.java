package com.yunlan.dto;

import lombok.Data;

@Data
public class TeamMemberVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer orderCount;
    private java.math.BigDecimal totalRebate;
    private String createTime;
}
