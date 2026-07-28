package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.dto.FreightDTO;
import com.yunlan.service.FreightService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

@RestController
@RequestMapping("/tasks")
@Api(tags = "运费模块")
public class FreightController {

    @Resource
    private FreightService freightService;

    @PostMapping("/calculate")
    @ApiOperation("计算运费")
    public Result<BigDecimal> calculate(@RequestBody FreightDTO dto) {
        return Result.success(freightService.calculate(dto));
    }
}
