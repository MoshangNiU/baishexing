package com.yunlan.dto;

import lombok.Data;

@Data
public class AddressBookDTO {
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String lat;
    private String lng;
    private Integer isDefault;
    private String label;
}
