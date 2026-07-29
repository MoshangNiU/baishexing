package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Orders;
import com.yunlan.entity.User;
import com.yunlan.mapper.OrdersMapper;
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
@RequestMapping("/admin-api/orders")
@Api(tags = "管理端 - 订单管理")
public class AdminOrderController {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private UserMapper userMapper;

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
        if (status != null) wrapper.eq(Orders::getStatus, status);
        if (paymentStatus != null) wrapper.eq(Orders::getPaymentStatus, paymentStatus);
        if (serveCategoryId != null) wrapper.eq(Orders::getServeCategoryId, serveCategoryId);
        wrapper.orderByDesc(Orders::getId);

        Page<Orders> p = ordersMapper.selectPage(new Page<>(page, pageSize), wrapper);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> matchedUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, keyword).or().like(User::getPhone, keyword));
            List<Long> userIds = matchedUsers.stream().map(User::getId).collect(Collectors.toList());
            wrapper.and(w -> {
                w.like(Orders::getContactsName, keyword)
                        .or().like(Orders::getContactsPhone, keyword);
                if (!userIds.isEmpty()) w.or().in(Orders::getUserId, userIds);
            });
            p = ordersMapper.selectPage(new Page<>(page, pageSize), wrapper);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("订单详情")
    public Result<Orders> detail(@PathVariable Long id) {
        return Result.success(ordersMapper.selectById(id));
    }

    @PutMapping("/dispatch")
    @ApiOperation("派单")
    public Result<Void> dispatch(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Long serverId = body.get("serverId") != null ? Long.valueOf(body.get("serverId").toString()) : null;
        String serverName = body.get("serverName") != null ? body.get("serverName").toString() : null;
        Orders o = new Orders();
        o.setId(id);
        o.setStatus(200);
        o.setServerName(serverName);
        ordersMapper.updateById(o);
        return Result.success();
    }

    @PutMapping("/close")
    @ApiOperation("关闭订单")
    public Result<Void> close(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Orders o = new Orders();
        o.setId(id);
        o.setStatus(700);
        ordersMapper.updateById(o);
        return Result.success();
    }

    @PutMapping("/redistribute")
    @ApiOperation("改派")
    public Result<Void> redistribute(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String serverName = body.get("serverName") != null ? body.get("serverName").toString() : null;
        Orders o = new Orders();
        o.setId(id);
        o.setServerName(serverName);
        ordersMapper.updateById(o);
        return Result.success();
    }
}
