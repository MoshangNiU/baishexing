package com.yunlan.dto;

import lombok.Data;

@Data
public class ServeSearchDTO {
    private String keyword;
    private Long categoryId;
    private Integer page;
    private Integer pageSize;
    private String cityCode;
    private String sortType;
}
