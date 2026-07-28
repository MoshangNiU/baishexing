package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("worker_recommend")
public class WorkerRecommend {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private Integer experienceYears;

    private String skills;

    private String description;

    private BigDecimal rating;

    private BigDecimal price;

    private Integer serveCount;

    private Integer status;

    private Long regionId;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
