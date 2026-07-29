package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Coupon;
import com.yunlan.entity.CouponActivity;
import com.yunlan.entity.User;
import com.yunlan.mapper.CouponActivityMapper;
import com.yunlan.mapper.CouponMapper;
import com.yunlan.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-api/coupon")
@Api(tags = "管理端 - 优惠券管理")
public class AdminCouponController {

    @Resource
    private CouponActivityMapper couponActivityMapper;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private UserMapper userMapper;

    @GetMapping("/activities/page")
    @ApiOperation("活动分页")
    public Result<Map<String, Object>> activitiesPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CouponActivity> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) w.like(CouponActivity::getName, keyword);
        w.orderByDesc(CouponActivity::getId);
        Page<CouponActivity> p = couponActivityMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PostMapping("/activity")
    @ApiOperation("新增活动")
    public Result<Long> addActivity(@RequestBody CouponActivity activity) {
        couponActivityMapper.insert(activity);
        return Result.success(activity.getId());
    }

    @PutMapping("/activity")
    @ApiOperation("修改活动")
    public Result<Void> updateActivity(@RequestBody CouponActivity activity) {
        couponActivityMapper.updateById(activity);
        return Result.success();
    }

    @DeleteMapping("/activity/{id}")
    @ApiOperation("删除活动")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        couponActivityMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/user/page")
    @ApiOperation("用户优惠券分页")
    public Result<Map<String, Object>> userCouponPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Coupon> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Coupon::getStatus, status);
        w.orderByDesc(Coupon::getId);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, keyword).or().like(User::getPhone, keyword));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            List<CouponActivity> acts = couponActivityMapper.selectList(
                    new LambdaQueryWrapper<CouponActivity>().like(CouponActivity::getName, keyword));
            List<Long> actIds = acts.stream().map(CouponActivity::getId).collect(Collectors.toList());
            w.and(ww -> {
                if (!userIds.isEmpty()) ww.in(Coupon::getUserId, userIds);
                if (!actIds.isEmpty()) ww.or().in(Coupon::getActivityId, actIds);
            });
        }

        Page<Coupon> p = couponMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }
}
