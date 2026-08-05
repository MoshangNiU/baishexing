package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("serve_category")
public class ServeCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String icon;
    private String image;
    private Integer sort;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
