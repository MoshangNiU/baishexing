package com.yunlan.enums;

public enum OrderStatusEnum {
    PENDING_PAYMENT(0, "待支付"),
    PENDING_SERVICE(1, "待服务"),
    IN_PROGRESS(2, "服务中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REFUNDING(5, "退款中");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
