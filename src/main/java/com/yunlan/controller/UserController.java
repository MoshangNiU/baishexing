package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.dto.RealNameVerifyDTO;
import com.yunlan.dto.UserDTO;
import com.yunlan.dto.UserProfileVO;
import com.yunlan.entity.User;
import com.yunlan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/customer/consumer")
@Api(tags = "用户模块")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/profile")
    @ApiOperation("获取用户信息(增强版)")
    public Result<UserProfileVO> getUserInfo() {
        return Result.success(userService.getCurrentUserProfile());
    }

    @PutMapping("/profile")
    @ApiOperation("修改用户信息")
    public Result<Void> updateUserInfo(@RequestBody UserDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success();
    }

    @PostMapping("/realNameVerify")
    @ApiOperation("用户实名认证")
    public Result<Void> verifyRealName(@RequestBody RealNameVerifyDTO dto) {
        userService.verifyRealName(dto);
        return Result.success();
    }

    @GetMapping("/get")
    @ApiOperation("获取用户信息（备用）")
    public Result<User> getUser() {
        return Result.success(userService.getCurrentUser());
    }
}
