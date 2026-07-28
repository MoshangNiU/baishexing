package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    @TableField(exist = false)
    private String password;
    private String token;
    private String openid;
    private Integer status;
    private Long inviterId;
    private String inviteCode;
    private java.math.BigDecimal totalRebate;
    private java.math.BigDecimal balance;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
