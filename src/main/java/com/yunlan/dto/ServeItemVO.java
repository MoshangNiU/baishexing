package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServeItemVO {
    private Long id;
    private String serveItemName;
    private String serveItemImg;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private Integer serveCount;
    private BigDecimal rating;
    private String description;
}
