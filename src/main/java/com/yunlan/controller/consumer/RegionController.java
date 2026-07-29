package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.entity.Region;
import com.yunlan.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/foundations/consumer/region")
@Api(tags = "区域模块（公开接口）")
public class RegionController {

    @Resource
    private RegionService regionService;

    @GetMapping("/activeRegionList")
    @ApiOperation("获取已开通的城市列表")
    public Result<List<Region>> activeRegionList() {
        return Result.success(regionService.getActiveRegionList());
    }

    @GetMapping("/queryRegionDisplayByCityCode")
    @ApiOperation("根据城市编码查询区域展示配置")
    public Result<Map<String, Object>> queryRegionDisplay(@RequestParam String cityCode) {
        return Result.success(regionService.queryRegionDisplayByCityCode(cityCode));
    }
}
