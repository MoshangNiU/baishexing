package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HotServeVO {
    private Long id;
    private String serveItemImg;
    private String serveItemName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private String description;
    private Integer serveCount;
    private BigDecimal rating;
}
