package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WorkerRecommendVO {
    private Long id;
    private String name;
    private String avatar;
    private Integer experienceYears;
    private String skills;
    private String description;
    private BigDecimal rating;
    private BigDecimal price;
    private Integer serveCount;
}
