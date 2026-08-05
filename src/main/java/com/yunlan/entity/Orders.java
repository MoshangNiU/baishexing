package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long serveItemId;
    private Long serveCategoryId;
    private Long addressId;
    private Integer status;
    private Integer paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private LocalDateTime serviceTime;
    private String remarks;
    private Long couponId;
    private Integer purNum;
    private String serveAddress;
    private String contactsName;
    private String contactsPhone;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String serverName;
    private Long workerId;
    private LocalDateTime serveActualEndTime;
    private LocalDateTime serveStartTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
