package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易单（支付流水）
 * 对应每一次支付请求，无论是小程序 JSAPI 还是 H5 MWEB
 */
@Data
@TableName("trading")
public class Trading {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 下单用户ID */
    private Long userId;

    /** 交易金额（元） */
    private BigDecimal totalAmount;

    /** 交易状态：0待支付 1支付成功 2已关闭 */
    private Integer status;

    /** 支付渠道：1微信 2支付宝 */
    private Integer payChannel;

    /** 业务交易单号（雪花ID，作为 out_trade_no 传给微信） */
    private Long tradingOrderNo;

    /** 客户端 IP（H5 支付必填） */
    private String clientIp;

    /** JSAPI 调起支付参数 JSON */
    private String placeOrderJson;

    /** H5 支付跳转 URL（mweb_url） */
    private String placeOrderMsg;

    /** 微信预支付ID（prepay_id，JSAPI 返回） */
    private String prepayId;

    /** 微信支付订单号（transaction_id，回调时写入） */
    private String transactionId;

    /** 交易类型：JSAPI / MWEB / NATIVE 等 */
    private String tradeType;

    /** 支付完成时间（回调时写入） */
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
