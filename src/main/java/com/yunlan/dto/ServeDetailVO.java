package com.yunlan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ServeDetailVO {
    private Long serveItemId;
    private String serveItemImg;
    private String serveItemName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private String duration;
    private String scope;
    private String tags;
    private String notice;
    private String description;
    private String detailImg;
    private List<String> detailImgList;
    private String categoryName;
    private List<String> carouselImages;
    private Integer serveCount;
    private BigDecimal rating;
}
