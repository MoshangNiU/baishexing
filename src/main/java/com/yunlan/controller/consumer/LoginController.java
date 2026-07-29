package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.dto.LoginDTO;
import com.yunlan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/customer/open/login")
@Api(tags = "登录模块")
public class LoginController {

    @Resource
    private UserService userService;

    @PostMapping("/common/user")
    @ApiOperation("手机号登录")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PutMapping("/common-user")
    @ApiOperation("获取手机号")
    public Result<Void> getPhone(@RequestParam String phoneCode) {
        userService.getPhone(phoneCode);
        return Result.success();
    }

    @PostMapping("/phone")
    @ApiOperation("手机号登录（备用）")
    public Result<Map<String, Object>> phoneLogin(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Map<String, Object>> register(@RequestBody LoginDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/password")
    @ApiOperation("密码登录")
    public Result<Map<String, Object>> passwordLogin(@RequestBody LoginDTO dto) {
        return Result.success(userService.passwordLogin(dto));
    }

    @PostMapping("/wechat")
    @ApiOperation("微信小程序登录/注册（code换openid，新用户自动注册）")
    public Result<Map<String, Object>> wechatLogin(@RequestBody LoginDTO dto) {
        return Result.success(userService.wechatLogin(dto));
    }

    @PostMapping("/sms")
    @ApiOperation("短信验证码登录/注册（手机号+验证码，新用户自动注册）")
    public Result<Map<String, Object>> smsLogin(@RequestBody LoginDTO dto) {
        return Result.success(userService.smsLogin(dto));
    }

    @PostMapping("/account")
    @ApiOperation("账号登录")
    public Result<Map<String, Object>> accountLogin(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }
}
