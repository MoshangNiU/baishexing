package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/address")
@Api(tags = "地址查询模块")
public class LocationController {

    @Resource
    private AddressBookService addressBookService;

    @GetMapping("/findDetailByLocation")
    @ApiOperation("根据经纬度查询详细地址")
    public Result<Map<String, Object>> findDetailByLocation(@RequestParam String lat,
                                                            @RequestParam String lng) {
        return Result.success(addressBookService.findDetailByLocation(lat, lng));
    }
}
