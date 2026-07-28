package com.yunlan.dto;

import lombok.Data;

@Data
public class PageDTO {
    private Integer page = 1;
    private Integer pageSize = 10;

    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }
}
