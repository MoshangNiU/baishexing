package com.yunlan.dto;

import lombok.Data;

@Data
public class CancelOrderDTO {
    private Long id;
    private String cancelReason;
    private String cancelType;
}
