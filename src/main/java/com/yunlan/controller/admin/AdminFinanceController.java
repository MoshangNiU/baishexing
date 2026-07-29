package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.*;
import com.yunlan.mapper.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-api/finance")
@Api(tags = "管理端 - 财务管理")
public class AdminFinanceController {

    @Resource
    private TradingMapper tradingMapper;
    @Resource
    private WithdrawalMapper withdrawalMapper;
    @Resource
    private RebateRecordMapper rebateRecordMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private OrdersMapper ordersMapper;

    private Map<String, Object> buildPageResult(Page<?> p, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @GetMapping("/tradings/page")
    @ApiOperation("交易流水分页")
    public Result<Map<String, Object>> tradingsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Trading> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Trading::getStatus, status);
        w.orderByDesc(Trading::getId);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, keyword).or().like(User::getPhone, keyword));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            w.and(ww -> {
                ww.like(Trading::getTradingOrderNo, keyword);
                if (!userIds.isEmpty()) ww.or().in(Trading::getUserId, userIds);
            });
        }

        Page<Trading> p = tradingMapper.selectPage(new Page<>(page, pageSize), w);
        return Result.success(buildPageResult(p, page, pageSize));
    }

    @GetMapping("/withdrawals/page")
    @ApiOperation("提现申请分页")
    public Result<Map<String, Object>> withdrawalsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Withdrawal> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Withdrawal::getStatus, status);
        w.orderByDesc(Withdrawal::getId);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, keyword).or().like(User::getPhone, keyword));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            if (!userIds.isEmpty()) w.in(Withdrawal::getUserId, userIds);
        }

        Page<Withdrawal> p = withdrawalMapper.selectPage(new Page<>(page, pageSize), w);
        return Result.success(buildPageResult(p, page, pageSize));
    }

    @PutMapping("/withdrawals/audit")
    @ApiOperation("审核提现")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> auditWithdrawal(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        String remark = body.get("remark") != null ? body.get("remark").toString() : "";

        Withdrawal wd = withdrawalMapper.selectById(id);
        if (wd == null || wd.getStatus() != 0) {
            return Result.error("状态异常");
        }

        wd.setStatus(status);
        wd.setAuditRemark(remark);
        wd.setAuditTime(LocalDateTime.now());
        withdrawalMapper.updateById(wd);

        if (status == 2) {
            User user = userMapper.selectById(wd.getUserId());
            if (user != null) {
                user.setBalance(user.getBalance().add(wd.getAmount()));
                userMapper.updateById(user);
            }
        }
        return Result.success();
    }

    @GetMapping("/rebates/page")
    @ApiOperation("返利记录分页")
    public Result<Map<String, Object>> rebatesPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<RebateRecord> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(RebateRecord::getStatus, status);
        w.orderByDesc(RebateRecord::getId);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, keyword).or().like(User::getPhone, keyword));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            w.and(ww -> {
                if (!userIds.isEmpty()) ww.in(RebateRecord::getUserId, userIds)
                        .or().in(RebateRecord::getInviterId, userIds);
            });
        }

        Page<RebateRecord> p = rebateRecordMapper.selectPage(new Page<>(page, pageSize), w);
        return Result.success(buildPageResult(p, page, pageSize));
    }

    @PutMapping("/rebates/settle")
    @ApiOperation("结算返利")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> settleRebate(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        RebateRecord rr = rebateRecordMapper.selectById(id);
        if (rr == null || rr.getStatus() != 0) {
            return Result.error("状态异常");
        }
        rr.setStatus(1);
        rebateRecordMapper.updateById(rr);

        User user = userMapper.selectById(rr.getInviterId());
        if (user != null) {
            user.setBalance(user.getBalance() != null ? user.getBalance().add(rr.getAmount()) : rr.getAmount());
            user.setTotalRebate(user.getTotalRebate() != null ? user.getTotalRebate().add(rr.getAmount()) : rr.getAmount());
            userMapper.updateById(user);
        }
        return Result.success();
    }
}
