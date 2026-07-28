package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.entity.Area;
import com.yunlan.service.AreaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/areas")
@Api(tags = "省市区模块（公开接口）")
public class AreaController {

    @Resource
    private AreaService areaService;

    @GetMapping("/children")
    @ApiOperation("获取省市区子级列表")
    public Result<List<Area>> getChildren(@RequestParam(defaultValue = "0") Long parentId) {
        return Result.success(areaService.getChildren(parentId));
    }
}
