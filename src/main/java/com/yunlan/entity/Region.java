package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("region")
public class Region {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cityCode;
    private String cityName;
    private String province;
    private String provinceCode;
    private Integer activeStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
