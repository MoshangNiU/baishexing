package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.CouponVO;
import com.yunlan.entity.Coupon;
import com.yunlan.entity.CouponActivity;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    List<CouponActivity> getActivityList(int page, int pageSize);
    void seizeCoupon(Long activityId);
    List<CouponVO> getMyCouponList(int page, int pageSize, Integer status);
    List<CouponActivity> getAvailableCoupons(Long orderId);
}
