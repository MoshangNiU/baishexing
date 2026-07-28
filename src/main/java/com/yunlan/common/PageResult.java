package com.yunlan.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private long page;
    private long pageSize;
    private List<T> records;

    public static <T> PageResult<T> of(long total, long page, long pageSize, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setRecords(records);
        return result;
    }
}
