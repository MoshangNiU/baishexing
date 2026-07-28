package com.yunlan.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CouponPageDTO extends PageDTO {
    private Integer status;
}
