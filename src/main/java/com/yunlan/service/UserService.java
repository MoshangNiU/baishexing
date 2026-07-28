package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.LoginDTO;
import com.yunlan.dto.RealNameVerifyDTO;
import com.yunlan.dto.UserDTO;
import com.yunlan.dto.UserProfileVO;
import com.yunlan.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginDTO dto);
    Map<String, Object> register(LoginDTO dto);
    Map<String, Object> passwordLogin(LoginDTO dto);
    Map<String, Object> smsLogin(LoginDTO dto);
    Map<String, Object> wechatLogin(LoginDTO dto);
    void getPhone(String phoneCode);
    User getCurrentUser();
    UserProfileVO getCurrentUserProfile();
    void updateUserInfo(UserDTO dto);
    void verifyRealName(RealNameVerifyDTO dto);
}
