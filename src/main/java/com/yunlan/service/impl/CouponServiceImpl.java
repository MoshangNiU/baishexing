package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.CouponVO;
import com.yunlan.entity.Coupon;
import com.yunlan.entity.CouponActivity;
import com.yunlan.mapper.CouponActivityMapper;
import com.yunlan.mapper.CouponMapper;
import com.yunlan.service.CouponService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Resource
    private CouponActivityMapper couponActivityMapper;

    @Override
    public List<CouponActivity> getActivityList(int page, int pageSize) {
        return couponActivityMapper.selectList(new LambdaQueryWrapper<CouponActivity>()
                .eq(CouponActivity::getStatus, 1)
                .orderByDesc(CouponActivity::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize));
    }

    @Override
    @Transactional
    public void seizeCoupon(Long activityId) {
        Long userId = UserHolder.get();
        // Check if user already claimed this coupon
        Long count = this.lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(Coupon::getActivityId, activityId)
                .count();
        if (count > 0) {
            throw new IllegalStateException("您已领取过该优惠券");
        }

        CouponActivity activity = couponActivityMapper.selectById(activityId);
        if (activity == null) throw new IllegalArgumentException("活动不存在");
        if (activity.getRemainCount() == null || activity.getRemainCount() <= 0) {
            throw new IllegalStateException("优惠券已被抢完");
        }
        // 扣减库存
        activity.setRemainCount(activity.getRemainCount() - 1);
        couponActivityMapper.updateById(activity);

        Coupon coupon = new Coupon();
        coupon.setUserId(userId);
        coupon.setActivityId(activityId);
        coupon.setStatus(1);
        this.save(coupon);
    }

    @Override
    public List<CouponVO> getMyCouponList(int page, int pageSize, Integer status) {
        Long userId = UserHolder.get();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getUserId, userId)
                .orderByDesc(Coupon::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize);
        if (status != null) {
            wrapper.eq(Coupon::getStatus, status);
        }
        List<Coupon> coupons = this.list(wrapper);
        if (coupons.isEmpty()) return new ArrayList<>();

        // Enrich with CouponActivity data
        List<Long> activityIds = coupons.stream().map(Coupon::getActivityId).collect(Collectors.toList());
        List<CouponActivity> activities = couponActivityMapper.selectList(
                new LambdaQueryWrapper<CouponActivity>().in(CouponActivity::getId, activityIds)
        );
        java.util.Map<Long, CouponActivity> activityMap = activities.stream()
                .collect(Collectors.toMap(CouponActivity::getId, a -> a));

        return coupons.stream().map(c -> {
            CouponVO vo = new CouponVO();
            vo.setId(c.getId());
            vo.setActivityId(c.getActivityId());
            vo.setStatus(c.getStatus());
            vo.setCreateTime(c.getCreateTime());
            CouponActivity act = activityMap.get(c.getActivityId());
            if (act != null) {
                vo.setName(act.getName());
                vo.setDiscountAmount(act.getDiscountAmount());
                vo.setConditionAmount(act.getConditionAmount());
                vo.setDescription(act.getDescription());
                vo.setEndTime(act.getEndTime());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CouponActivity> getAvailableCoupons(Long orderId) {
        // 模拟返回当前订单可用的优惠券活动
        return couponActivityMapper.selectList(new LambdaQueryWrapper<CouponActivity>()
                .eq(CouponActivity::getStatus, 1));
    }
}
