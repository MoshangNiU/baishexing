package com.yunlan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlaceOrderDTO {
    @JsonAlias("serveId")
    private Long serveItemId;
    @JsonAlias("addressBookId")
    private Long addressId;
    private Long serveCategoryId;
    private Long couponId;
    private LocalDateTime serveStartTime;
    private Integer purNum;
    private String remarks;
    private String appointmentType;
    private String contactsName;
    private String contactsPhone;
}
