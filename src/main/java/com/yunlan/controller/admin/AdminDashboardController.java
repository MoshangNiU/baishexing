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
import java.math.RoundingMode;
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
    private ServeCategoryMapper serveCategoryMapper;
    @Resource
    private WithdrawalMapper withdrawalMapper;

    @GetMapping("/kpi")
    @ApiOperation("核心指标")
    public Result<Map<String, Object>> kpi() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<Orders> todayW = new LambdaQueryWrapper<>();
        todayW.between(Orders::getCreateTime, todayStart, todayEnd);
        List<Orders> todayOrders = ordersMapper.selectList(todayW);
        long todayOrder = todayOrders.size();
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(o -> o.getPaymentStatus() != null && o.getPaymentStatus() != 0)
                .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalUser = userMapper.selectCount(null);
        long totalWorker = workerRecommendMapper.selectCount(
                new LambdaQueryWrapper<WorkerRecommend>().eq(WorkerRecommend::getStatus, 1));

        long pendingDispatch = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, 1).eq(Orders::getPaymentStatus, 0));
        long pendingWorkerAudit = workerRecommendMapper.selectCount(
                new LambdaQueryWrapper<WorkerRecommend>().eq(WorkerRecommend::getStatus, 0));
        long pendingWithdraw = withdrawalMapper.selectCount(
                new LambdaQueryWrapper<Withdrawal>().eq(Withdrawal::getStatus, 0));

        long completed = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, 3));
        long allOrders = ordersMapper.selectCount(null);
        int completionRate = allOrders > 0 ? (int) (completed * 100 / allOrders) : 0;

        Map<String, Object> data = new HashMap<>();
        data.put("todayOrder", todayOrder);
        data.put("todayRevenue", todayRevenue);
        data.put("totalUser", totalUser);
        data.put("totalWorker", totalWorker);
        data.put("completionRate", completionRate);
        data.put("pendingDispatch", pendingDispatch);
        data.put("pendingWorkerAudit", pendingWorkerAudit);
        data.put("pendingWithdraw", pendingWithdraw);
        return Result.success(data);
    }

    @GetMapping("/order-trend")
    @ApiOperation("订单趋势")
    public Result<List<Map<String, Object>>> orderTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
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
            m.put("order", (long) list.size());
            m.put("revenue", amount);
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
            m.put("name", c.getName());
            m.put("value", count);
            result.add(m);
        }
        return Result.success(result);
    }

    @GetMapping("/status")
    @ApiOperation("订单状态分布")
    public Result<List<Map<String, Object>>> statusDist() {
        List<Orders> all = ordersMapper.selectList(null);
        Map<Integer, Long> grouped = all.stream()
                .collect(Collectors.groupingBy(o -> {
                    Integer s = o.getStatus() != null ? o.getStatus() : 0;
                    Integer ps = o.getPaymentStatus();
                    if (s == 0) return 0;
                    if (s == 1 && ps != null && ps == 0) return 100;
                    if (s == 1 && ps != null && ps == 1) return 200;
                    if (s == 2) return 300;
                    if (s == 3) return 400;
                    if (s == 4) return 600;
                    return s;
                }, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> e : grouped.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("status", e.getKey());
            m.put("count", e.getValue());
            result.add(m);
        }
        return Result.success(result);
    }
}
