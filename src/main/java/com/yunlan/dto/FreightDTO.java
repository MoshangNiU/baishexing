package com.yunlan.dto;

import lombok.Data;

@Data
public class FreightDTO {
    private Long serveItemId;
    private Long addressId;
    private String serveType;
    private Long cityCode;
}
