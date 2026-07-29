package com.yunlan.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunlan.common.Result;
import com.yunlan.entity.*;
import com.yunlan.mapper.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-api/dashboard")
@Api(tags = "管理端 - 数据面板")
public class AdminDashboardController {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private WorkerRecommendMapper workerRecommendMapper;
    @Resource
    private ServeItemMapper serveItemMapper;
    @Resource
    private ServeCategoryMapper serveCategoryMapper;

    @GetMapping("/kpi")
    @ApiOperation("核心指标")
    public Result<Map<String, Object>> kpi() {
        Map<String, Object> data = new HashMap<>();
        data.put("orderCount", ordersMapper.selectCount(null));
        LambdaQueryWrapper<Orders> paid = new LambdaQueryWrapper<>();
        paid.ne(Orders::getPaymentStatus, 0);
        List<Orders> paidOrders = ordersMapper.selectList(paid);
        BigDecimal total = paidOrders.stream()
                .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("turnover", total);
        data.put("userCount", userMapper.selectCount(null));
        data.put("workerCount", workerRecommendMapper.selectCount(null));
        return Result.success(data);
    }

    @GetMapping("/order-trend")
    @ApiOperation("订单趋势")
    public Result<List<Map<String, Object>>> orderTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<>();
            w.between(Orders::getCreateTime, start, end);
            List<Orders> list = ordersMapper.selectList(w);
            BigDecimal amount = list.stream()
                    .filter(o -> o.getPaymentStatus() != null && o.getPaymentStatus() != 0)
                    .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new HashMap<>();
            m.put("date", day.format(DateTimeFormatter.ISO_LOCAL_DATE));
            m.put("orderCount", (long) list.size());
            m.put("turnover", amount);
            result.add(m);
        }
        return Result.success(result);
    }

    @GetMapping("/category")
    @ApiOperation("分类销量")
    public Result<List<Map<String, Object>>> category() {
        List<ServeCategory> categories = serveCategoryMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServeCategory c : categories) {
            LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<>();
            w.eq(Orders::getServeCategoryId, c.getId());
            Long count = ordersMapper.selectCount(w);
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("count", count);
            result.add(m);
        }
        return Result.success(result);
    }

    @GetMapping("/status")
    @ApiOperation("订单状态分布")
    public Result<Map<String, Long>> statusDist() {
        List<Orders> all = ordersMapper.selectList(null);
        Map<String, Long> map = all.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus().toString() : "0", Collectors.counting()));
        return Result.success(map);
    }
}
