package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.dto.SmsCodeDTO;
import com.yunlan.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/verifyCodes")
@Api(tags = "验证码模块（公开接口）")
public class VerifyCodeController {

    @Resource
    private SmsService smsService;

    @PostMapping("/smsCode")
    @ApiOperation("发送短信验证码")
    public Result<Void> sendSmsCode(@RequestBody SmsCodeDTO dto) {
        smsService.sendCode(dto.getPhone());
        return Result.success();
    }
}
