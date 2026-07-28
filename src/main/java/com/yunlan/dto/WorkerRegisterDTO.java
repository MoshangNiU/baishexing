package com.yunlan.dto;

import lombok.Data;

@Data
public class WorkerRegisterDTO {
    private String name;
    private String phone;
    private String avatar;
    private Integer experienceYears;
    private String skills;
    private String description;
    private Long regionId;
}
