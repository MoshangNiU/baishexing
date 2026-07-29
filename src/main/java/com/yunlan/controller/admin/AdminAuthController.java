package com.yunlan.controller.admin;

import com.yunlan.common.Result;
import com.yunlan.entity.AdminUser;
import com.yunlan.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/auth")
@Api(tags = "管理端 - 认证")
public class AdminAuthController {

    @Resource
    private AdminUserService adminUserService;

    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return Result.success(adminUserService.login(username, password));
    }

    @PostMapping("/logout")
    @ApiOperation("管理员登出")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/profile")
    @ApiOperation("获取管理员信息")
    public Result<AdminUser> profile() {
        return Result.success(adminUserService.getCurrentAdmin());
    }
}
