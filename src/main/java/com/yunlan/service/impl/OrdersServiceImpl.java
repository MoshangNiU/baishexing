package com.yunlan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.CancelOrderDTO;
import com.yunlan.dto.OrderDetailVO;
import com.yunlan.dto.OrderVO;
import com.yunlan.dto.PayOrderDTO;
import com.yunlan.dto.PlaceOrderDTO;
import com.yunlan.entity.AddressBook;
import com.yunlan.entity.Coupon;
import com.yunlan.entity.CouponActivity;
import com.yunlan.entity.Orders;
import com.yunlan.entity.ServeCategory;
import com.yunlan.entity.ServeItem;
import com.yunlan.enums.OrderStatusEnum;
import com.yunlan.mapper.OrdersMapper;
import com.yunlan.mapper.CouponMapper;
import com.yunlan.mapper.CouponActivityMapper;
import com.yunlan.service.AddressBookService;
import com.yunlan.service.OrdersService;
import com.yunlan.service.ServeCategoryService;
import com.yunlan.service.ServeItemService;
import com.yunlan.service.NotificationService;
import com.yunlan.service.DistributionService;
import com.yunlan.service.UserService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Resource
    private ServeItemService serveItemService;

    @Resource
    private ServeCategoryService serveCategoryService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private DistributionService distributionService;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private CouponActivityMapper couponActivityMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional
    public Orders placeOrder(PlaceOrderDTO dto) {
        Long userId = UserHolder.get();
        Orders order = new Orders();
        order.setUserId(userId);
        order.setServeItemId(dto.getServeItemId());
        order.setServeCategoryId(dto.getServeCategoryId());
        order.setAddressId(dto.getAddressId());
        order.setCouponId(dto.getCouponId());
        order.setRemarks(dto.getRemarks());
        order.setPurNum(dto.getPurNum() != null ? dto.getPurNum() : 1);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setPaymentStatus(0);

        // Calculate total amount from serve item price
        BigDecimal totalAmount = BigDecimal.ZERO;
        ServeItem item = serveItemService.getById(dto.getServeItemId());
        if (item != null && item.getPrice() != null) {
            totalAmount = item.getPrice().multiply(BigDecimal.valueOf(order.getPurNum()));
        }
        order.setTotalAmount(totalAmount);

        // Calculate actual amount with coupon discount
        BigDecimal actualAmount = totalAmount;
        if (dto.getCouponId() != null) {
            try {
                Coupon coupon = couponMapper.selectById(dto.getCouponId());
                if (coupon != null && coupon.getStatus() == 1) {
                    CouponActivity act = couponActivityMapper.selectById(coupon.getActivityId());
                    if (act != null && act.getDiscountAmount() != null) {
                        actualAmount = totalAmount.subtract(act.getDiscountAmount());
                        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
                            actualAmount = BigDecimal.ZERO;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        order.setActualAmount(actualAmount);

        // 填充地址相关信息
        if (dto.getAddressId() != null) {
            AddressBook addr = addressBookService.getById(dto.getAddressId());
            if (addr != null) {
                order.setServeAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetailAddress());
                order.setContactsName(addr.getName());
                order.setContactsPhone(addr.getPhone());
            }
        } else {
            order.setContactsName(dto.getContactsName());
            order.setContactsPhone(dto.getContactsPhone());
        }
        // 填充服务时间
        if (dto.getServeStartTime() != null) {
            order.setServeStartTime(dto.getServeStartTime());
        }

        this.save(order);

        // Create notification for new order
        try {
            notificationService.createNotification(
                    userId,
                    "订单已创建",
                    "您的订单已创建，请尽快完成支付",
                    "ORDER",
                    order.getId()
            );
        } catch (Exception ignored) {
        }

        return order;
    }

    @Override
    public List<OrderVO> getOrderPage(int page, int pageSize, Integer ordersStatus) {
        Long userId = UserHolder.get();
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize);
        if (ordersStatus != null) {
            wrapper.eq(Orders::getStatus, mapFrontendStatusToBackend(ordersStatus));
        }
        List<Orders> orders = this.list(wrapper);
        return orders.stream().map(this::convertToOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return convertToOrderDetailVO(order);
    }

    @Override
    public void cancelOrder(CancelOrderDTO dto) {
        Long userId = UserHolder.get();
        Orders order = this.getById(dto.getId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(dto.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        this.updateById(order);

        // Notification for cancellation
        try {
            notificationService.createNotification(
                    userId,
                    "订单已取消",
                    "您的订单 #" + order.getId() + " 已成功取消",
                    "ORDER",
                    order.getId()
            );
        } catch (Exception ignored) {
        }
    }

    @Override
    public void hideOrder(Long id) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order != null && order.getUserId().equals(userId)) {
            this.removeById(id);
        }
    }

    @Override
    @Transactional
    public void payOrder(Long id, PayOrderDTO dto) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        // Apply coupon selected at payment time (overrides any coupon from placeOrder)
        Long finalCouponId = order.getCouponId();
        if (dto.getCouponId() != null) {
            // Release previously applied coupon if different
            if (finalCouponId != null && !finalCouponId.equals(dto.getCouponId())) {
                try {
                    Coupon oldCoupon = couponMapper.selectById(finalCouponId);
                    if (oldCoupon != null && oldCoupon.getStatus() == 2) {
                        oldCoupon.setStatus(1);
                        couponMapper.updateById(oldCoupon);
                    }
                } catch (Exception ignored) {}
            }
            finalCouponId = dto.getCouponId();
            order.setCouponId(finalCouponId);
        }

        order.setPaymentStatus(1);
        order.setStatus(OrderStatusEnum.PENDING_SERVICE.getCode());

        // Recalculate actual amount based on coupon at payment time
        BigDecimal actualAmount = order.getTotalAmount();
        if (finalCouponId != null) {
            try {
                Coupon coupon = couponMapper.selectById(finalCouponId);
                if (coupon != null && coupon.getStatus() == 1) {
                    CouponActivity act = couponActivityMapper.selectById(coupon.getActivityId());
                    if (act != null && act.getDiscountAmount() != null) {
                        actualAmount = order.getTotalAmount().subtract(act.getDiscountAmount());
                        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
                            actualAmount = BigDecimal.ZERO;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        order.setActualAmount(actualAmount);
        this.updateById(order);

        // Mark coupon as used
        if (finalCouponId != null) {
            try {
                Coupon coupon = couponMapper.selectById(finalCouponId);
                if (coupon != null && coupon.getStatus() == 1) {
                    coupon.setStatus(2);
                    couponMapper.updateById(coupon);
                }
            } catch (Exception ignored) {
            }
        }

        // Notification for payment success
        try {
            notificationService.createNotification(
                    userId,
                    "支付成功",
                    "您的订单 #" + order.getId() + " 已支付成功，等待安排服务",
                    "ORDER",
                    order.getId()
            );
        } catch (Exception ignored) {
        }

        // Create rebate if user has inviter
        try {
            com.yunlan.entity.User currentUser = userService.getById(userId);
            if (currentUser != null && currentUser.getInviterId() != null) {
                BigDecimal rebateAmount = order.getActualAmount()
                        .multiply(new BigDecimal("0.05"))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                if (rebateAmount.compareTo(BigDecimal.ZERO) > 0) {
                    distributionService.createRebateForOrder(
                            order.getId(), userId, currentUser.getInviterId(), rebateAmount
                    );
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<OrderVO> consumerQueryList(Long lastId, Integer pageSize, Integer ordersStatus) {
        Long userId = UserHolder.get();
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getId);

        if (lastId != null && lastId > 0) {
            wrapper.lt(Orders::getId, lastId);
        }
        if (ordersStatus != null) {
            wrapper.eq(Orders::getStatus, mapFrontendStatusToBackend(ordersStatus));
        }
        wrapper.last("LIMIT " + (pageSize != null ? pageSize : 10));
        List<Orders> orders = this.list(wrapper);
        return orders.stream().map(this::convertToOrderVO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPayResult(Long id) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (order != null && order.getUserId().equals(userId)) {
            result.put("status", order.getPaymentStatus());
            result.put("payStatus", order.getPaymentStatus() == 1 ? "SUCCESS" : "PENDING");
        }
        return result;
    }

    @Override
    public OrderVO convertToOrderVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setServeItemId(order.getServeItemId());
        vo.setOrdersStatus(mapBackendStatusToFrontend(order));
        vo.setPayStatus(order.getPaymentStatus());
        vo.setPurNum(order.getPurNum());
        vo.setServeAddress(order.getServeAddress());
        vo.setContactsName(order.getContactsName());
        vo.setContactsPhone(order.getContactsPhone());
        vo.setServerName(order.getServerName());
        vo.setServeStartTime(order.getServeStartTime());
        vo.setServeActualEndTime(order.getServeActualEndTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setPrice(order.getTotalAmount());
        vo.setRealPayAmount(order.getActualAmount());
        vo.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        // 从关联表填充服务项信息
        if (order.getServeItemId() != null) {
            ServeItem item = serveItemService.getById(order.getServeItemId());
            if (item != null) {
                vo.setServeItemImg(item.getImage());
                vo.setServeItemName(item.getName());
                vo.setUnit(item.getUnit());
            }
        }
        if (order.getServeCategoryId() != null) {
            ServeCategory cat = serveCategoryService.getById(order.getServeCategoryId());
            if (cat != null) {
                vo.setServeTypeName(cat.getName());
            }
        }
        return vo;
    }

    private OrderDetailVO convertToOrderDetailVO(Orders order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setServeItemId(order.getServeItemId());
        vo.setOrdersStatus(mapBackendStatusToFrontend(order));
        vo.setPayStatus(order.getPaymentStatus());
        vo.setPurNum(order.getPurNum());
        vo.setServeAddress(order.getServeAddress());
        vo.setContactsName(order.getContactsName());
        vo.setContactsPhone(order.getContactsPhone());
        vo.setServerName(order.getServerName());
        vo.setServeStartTime(order.getServeStartTime());
        vo.setServeActualEndTime(order.getServeActualEndTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setRemarks(order.getRemarks());
        vo.setCancelTime(order.getCancelTime() != null ? order.getCancelTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        vo.setPrice(order.getTotalAmount());
        vo.setRealPayAmount(order.getActualAmount());
        vo.setCreateTime(order.getCreateTime());

        // 从关联表填充
        if (order.getServeItemId() != null) {
            ServeItem item = serveItemService.getById(order.getServeItemId());
            if (item != null) {
                vo.setServeItemImg(item.getImage());
                vo.setServeItemName(item.getName());
                vo.setUnit(item.getUnit());
            }
        }
        if (order.getServeCategoryId() != null) {
            ServeCategory cat = serveCategoryService.getById(order.getServeCategoryId());
            if (cat != null) {
                vo.setServeTypeName(cat.getName());
            }
        }
        return vo;
    }

    /**
     * 后端订单状态映射为前端订单状态
     * 前端: 0待支付 100派单中 200待服务 300服务中 400待评价 500已完成 600已取消 700已关闭
     * 后端: 0待支付 1待服务 2服务中 3已完成 4已取消
     */
    private Integer mapBackendStatusToFrontend(Orders order) {
        Integer status = order.getStatus();
        Integer payStatus = order.getPaymentStatus();
        if (status == 0) return 0;           // 待支付
        if (status == 1 && payStatus == 1) return 200;  // 待服务(已支付)
        if (status == 2) return 300;         // 服务中
        if (status == 3) return 400;         // 待评价
        if (status == 4) return 600;         // 已取消
        if (status == 1 && payStatus == 0) return 100;  // 派单中
        return 0;
    }

    private Integer mapFrontendStatusToBackend(Integer frontendStatus) {
        if (frontendStatus == null) return null;
        if (frontendStatus == 0) return 0;
        if (frontendStatus == 100 || frontendStatus == 200) return 1;
        if (frontendStatus == 300) return 2;
        if (frontendStatus == 400 || frontendStatus == 500) return 3;
        if (frontendStatus == 600 || frontendStatus == 700) return 4;
        return null;
    }
}
