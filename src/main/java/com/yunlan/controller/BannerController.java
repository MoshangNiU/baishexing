package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.entity.Banner;
import com.yunlan.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/foundations/consumer/banner")
@Api(tags = "轮播图模块（公开接口）")
public class BannerController {

    @Resource
    private BannerService bannerService;

    @GetMapping("/list")
    @ApiOperation("获取首页轮播图列表")
    public Result<List<Banner>> list() {
        return Result.success(bannerService.getActiveBanners());
    }
}
