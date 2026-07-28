package com.yunlan.service;

public interface SmsService {
    void sendCode(String phone);
    void verifyCode(String phone, String code);
}
