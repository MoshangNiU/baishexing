package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Orders;
import com.yunlan.entity.ServeCategory;
import com.yunlan.entity.ServeItem;
import com.yunlan.entity.User;
import com.yunlan.enums.OrderStatusEnum;
import com.yunlan.mapper.OrdersMapper;
import com.yunlan.mapper.ServeCategoryMapper;
import com.yunlan.mapper.ServeItemMapper;
import com.yunlan.mapper.UserMapper;
import com.yunlan.mapper.WorkerRecommendMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-api/orders")
@Api(tags = "管理端 - 订单管理")
public class AdminOrderController {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ServeItemMapper serveItemMapper;
    @Resource
    private ServeCategoryMapper serveCategoryMapper;
    @Resource
    private WorkerRecommendMapper workerRecommendMapper;

    private Integer mapBackendStatusToFrontend(Orders order) {
        Integer status = order.getStatus();
        Integer payStatus = order.getPaymentStatus();
        if (status == null) return 0;
        if (status == 0) return 0;
        if (status == 1 && payStatus != null && payStatus == 0) return 100;
        if (status == 1 && payStatus != null && payStatus == 1) return 200;
        if (status == 2) return 300;
        if (status == 3) return 400;
        if (status == 4) return 600;
        return status;
    }

    private Integer mapFrontendStatusToBackend(Integer frontendStatus) {
        if (frontendStatus == null) return null;
        if (frontendStatus == 0) return 0;
        if (frontendStatus == 100 || frontendStatus == 200) return 1;
        if (frontendStatus == 300) return 2;
        if (frontendStatus == 400 || frontendStatus == 500) return 3;
        if (frontendStatus == 600 || frontendStatus == 700) return 4;
        return frontendStatus;
    }

    @GetMapping("/page")
    @ApiOperation("订单分页")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Long serveCategoryId,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        Integer backendStatus = mapFrontendStatusToBackend(status);
        if (backendStatus != null) wrapper.eq(Orders::getStatus, backendStatus);
        if (paymentStatus != null) wrapper.eq(Orders::getPaymentStatus, paymentStatus);
        if (serveCategoryId != null) wrapper.eq(Orders::getServeCategoryId, serveCategoryId);
        wrapper.orderByDesc(Orders::getId);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> matchedUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, keyword).or().like(User::getPhone, keyword));
            List<Long> userIds = matchedUsers.stream().map(User::getId).collect(Collectors.toList());
            wrapper.and(w -> {
                w.like(Orders::getContactsName, keyword)
                        .or().like(Orders::getContactsPhone, keyword);
                if (!userIds.isEmpty()) w.or().in(Orders::getUserId, userIds);
            });
        }

        Page<Orders> p = ordersMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<Long> userIds = p.getRecords().stream().map(Orders::getUserId).distinct().collect(Collectors.toList());
        List<Long> itemIds = p.getRecords().stream().map(Orders::getServeItemId).filter(i -> i != null).distinct().collect(Collectors.toList());
        List<Long> catIds = p.getRecords().stream().map(Orders::getServeCategoryId).filter(i -> i != null).distinct().collect(Collectors.toList());

        Map<Long, String> userNames = new HashMap<>();
        Map<Long, String> userPhones = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> {
                userNames.put(u.getId(), u.getNickname());
                userPhones.put(u.getId(), u.getPhone());
            });
        }
        Map<Long, String> itemNames = new HashMap<>();
        if (!itemIds.isEmpty()) {
            serveItemMapper.selectBatchIds(itemIds).forEach(i -> itemNames.put(i.getId(), i.getName()));
        }
        Map<Long, String> catNames = new HashMap<>();
        if (!catIds.isEmpty()) {
            serveCategoryMapper.selectBatchIds(catIds).forEach(c -> catNames.put(c.getId(), c.getName()));
        }

        List<Map<String, Object>> list = p.getRecords().stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", "BSX" + String.format("%08d", o.getId()));
            m.put("userId", o.getUserId());
            m.put("userName", userNames.getOrDefault(o.getUserId(), "未知"));
            m.put("userPhone", userPhones.getOrDefault(o.getUserId(), ""));
            m.put("serveCategoryId", o.getServeCategoryId());
            m.put("serveCategoryName", o.getServeCategoryId() != null ? catNames.getOrDefault(o.getServeCategoryId(), "") : "");
            m.put("serveItemId", o.getServeItemId());
            m.put("serveItemName", o.getServeItemId() != null ? itemNames.getOrDefault(o.getServeItemId(), "") : "");
            m.put("status", mapBackendStatusToFrontend(o));
            m.put("paymentStatus", o.getPaymentStatus());
            m.put("totalAmount", o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            m.put("actualAmount", o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO);
            m.put("serveAddress", o.getServeAddress());
            m.put("contactsName", o.getContactsName());
            m.put("contactsPhone", o.getContactsPhone());
            m.put("serverName", o.getServerName());
            m.put("workerId", o.getWorkerId());
            m.put("serveStartTime", o.getServeStartTime());
            m.put("serveActualEndTime", o.getServeActualEndTime());
            m.put("serviceTime", o.getServiceTime());
            m.put("remarks", o.getRemarks());
            m.put("cancelReason", o.getCancelReason());
            m.put("cancelTime", o.getCancelTime());
            m.put("createTime", o.getCreateTime());
            m.put("purNum", o.getPurNum());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("订单详情")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Orders o = ordersMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        Map<String, Object> m = new HashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", "BSX" + String.format("%08d", o.getId()));
        m.put("userId", o.getUserId());
        User u = userMapper.selectById(o.getUserId());
        m.put("userName", u != null ? u.getNickname() : "未知");
        m.put("userPhone", u != null ? u.getPhone() : "");
        m.put("serveCategoryId", o.getServeCategoryId());
        m.put("serveCategoryName", o.getServeCategoryId() != null ?
                (serveCategoryMapper.selectById(o.getServeCategoryId()) != null ?
                        serveCategoryMapper.selectById(o.getServeCategoryId()).getName() : "") : "");
        m.put("serveItemId", o.getServeItemId());
        m.put("serveItemName", o.getServeItemId() != null ?
                (serveItemMapper.selectById(o.getServeItemId()) != null ?
                        serveItemMapper.selectById(o.getServeItemId()).getName() : "") : "");
        m.put("status", mapBackendStatusToFrontend(o));
        m.put("paymentStatus", o.getPaymentStatus());
        m.put("totalAmount", o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
        m.put("actualAmount", o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO);
        BigDecimal couponDiscount = o.getTotalAmount() != null && o.getActualAmount() != null
                ? o.getTotalAmount().subtract(o.getActualAmount()) : BigDecimal.ZERO;
        if (couponDiscount.compareTo(BigDecimal.ZERO) < 0) couponDiscount = BigDecimal.ZERO;
        m.put("couponDiscount", couponDiscount);
        BigDecimal price = (o.getPurNum() != null && o.getPurNum() > 0 && o.getActualAmount() != null)
                ? o.getActualAmount().divide(new BigDecimal(o.getPurNum()), 2, RoundingMode.HALF_UP)
                : (o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO);
        m.put("price", price);
        m.put("unit", "次");
        m.put("serveAddress", o.getServeAddress());
        m.put("contactsName", o.getContactsName());
        m.put("contactsPhone", o.getContactsPhone());
        m.put("serverId", o.getWorkerId());
        m.put("workerId", o.getWorkerId());
        m.put("serverName", o.getServerName());
        m.put("serveStartTime", o.getServeStartTime());
        m.put("serveActualStartTime", null);
        m.put("serveActualEndTime", o.getServeActualEndTime());
        m.put("serviceTime", o.getServiceTime());
        m.put("remarks", o.getRemarks());
        m.put("cancelReason", o.getCancelReason());
        m.put("cancelTime", o.getCancelTime());
        m.put("createTime", o.getCreateTime());
        m.put("purNum", o.getPurNum());
        return Result.success(m);
    }

    @PutMapping("/dispatch")
    @ApiOperation("派单")
    public Result<Void> dispatch(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Long workerId = body.get("workerId") != null ? Long.valueOf(body.get("workerId").toString()) : null;
        Orders o = ordersMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        if (workerId != null) {
            com.yunlan.entity.WorkerRecommend w = workerRecommendMapper.selectById(workerId);
            if (w != null) {
                o.setWorkerId(w.getId());
                o.setServerName(w.getName());
            }
        }
        o.setStatus(OrderStatusEnum.PENDING_SERVICE.getCode());
        o.setPaymentStatus(1);
        ordersMapper.updateById(o);
        return Result.success();
    }

    @PutMapping("/close")
    @ApiOperation("关闭订单")
    public Result<Void> close(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Orders o = new Orders();
        o.setId(id);
        o.setStatus(OrderStatusEnum.CANCELLED.getCode());
        ordersMapper.updateById(o);
        return Result.success();
    }

    @PutMapping("/redistribute")
    @ApiOperation("改派")
    public Result<Void> redistribute(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Long workerId = body.get("workerId") != null ? Long.valueOf(body.get("workerId").toString()) : null;
        Orders o = ordersMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        if (workerId != null) {
            com.yunlan.entity.WorkerRecommend w = workerRecommendMapper.selectById(workerId);
            if (w != null) {
                o.setWorkerId(w.getId());
                o.setServerName(w.getName());
            }
        }
        ordersMapper.updateById(o);
        return Result.success();
    }
}
