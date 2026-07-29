package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/publics/map")
@Api(tags = "地图模块（公开接口）")
public class MapController {

    @Resource
    private AddressBookService addressBookService;

    @GetMapping("/regeo")
    @ApiOperation("根据经纬度获取城市信息")
    public Result<Map<String, Object>> regeo(@RequestParam String location) {
        return Result.success(addressBookService.findDetailByLocation(
                location.split(",")[0], location.split(",")[1]));
    }
}
