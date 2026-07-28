package com.yunlan.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderPageDTO extends PageDTO {
    private Integer status;
    private Long serveItemId;
}
