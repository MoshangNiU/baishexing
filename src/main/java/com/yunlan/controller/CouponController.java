package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.entity.CouponActivity;
import com.yunlan.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/market/consumer")
@Api(tags = "优惠券模块")
public class CouponController {

    @Resource
    private CouponService couponService;

    @GetMapping("/activity/list")
    @ApiOperation("用户端抢券列表分页")
    public Result<List<CouponActivity>> activityList(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(couponService.getActivityList(page, pageSize));
    }

    @PostMapping("/coupon/seize")
    @ApiOperation("抢券")
    public Result<Void> seizeCoupon(@RequestParam Long activityId) {
        couponService.seizeCoupon(activityId);
        return Result.success();
    }

    @GetMapping("/coupon/my")
    @ApiOperation("我的优惠券列表")
    public Result<List<com.yunlan.dto.CouponVO>> myCouponList(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) Integer status) {
        return Result.success(couponService.getMyCouponList(page, pageSize, status));
    }
}
