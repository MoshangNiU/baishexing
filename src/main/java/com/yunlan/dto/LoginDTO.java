package com.yunlan.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String code;
    private String avatar;
    private String nickname;
    private String phone;
    private String phoneCode;
    private String password;
}
