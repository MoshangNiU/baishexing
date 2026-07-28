package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunlan.dto.DistributionInfoVO;
import com.yunlan.dto.RebateRecordVO;
import com.yunlan.dto.TeamMemberVO;
import com.yunlan.dto.WithdrawDTO;
import com.yunlan.entity.*;
import com.yunlan.mapper.*;
import com.yunlan.service.DistributionService;
import com.yunlan.service.UserService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistributionServiceImpl implements DistributionService {

    @Resource
    private UserService userService;

    @Resource
    private RebateRecordMapper rebateRecordMapper;

    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private ServeItemMapper serveItemMapper;

    @Override
    public DistributionInfoVO getMyDistributionInfo() {
        Long userId = UserHolder.get();
        User user = userService.getById(userId);
        if (user == null) return null;

        DistributionInfoVO vo = new DistributionInfoVO();
        vo.setInviteCode(user.getInviteCode());
        vo.setBalance(user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        vo.setTotalRebate(user.getTotalRebate() != null ? user.getTotalRebate() : BigDecimal.ZERO);

        Long teamCount = userService.lambdaQuery()
                .eq(User::getInviterId, userId)
                .count();
        vo.setTeamCount(teamCount.intValue());

        return vo;
    }

    @Override
    public List<RebateRecordVO> getRebateRecords(int page, int pageSize) {
        Long userId = UserHolder.get();
        int offset = (page - 1) * pageSize;

        List<RebateRecord> records = rebateRecordMapper.selectList(
                new LambdaQueryWrapper<RebateRecord>()
                        .eq(RebateRecord::getInviterId, userId)
                        .orderByDesc(RebateRecord::getCreateTime)
                        .last("LIMIT " + offset + ", " + pageSize)
        );

        return records.stream().map(r -> {
            RebateRecordVO vo = new RebateRecordVO();
            vo.setId(r.getId());
            vo.setOrderId(r.getOrderId());
            vo.setAmount(r.getAmount());
            vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreateTime());

            Orders order = ordersMapper.selectById(r.getOrderId());
            if (order != null && order.getServeItemId() != null) {
                ServeItem item = serveItemMapper.selectById(order.getServeItemId());
                if (item != null) {
                    vo.setServeItemName(item.getName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TeamMemberVO> getTeamMembers(int page, int pageSize) {
        Long userId = UserHolder.get();
        int offset = (page - 1) * pageSize;

        List<User> team = userService.lambdaQuery()
                .eq(User::getInviterId, userId)
                .last("LIMIT " + offset + ", " + pageSize)
                .list();

        return team.stream().map(u -> {
            TeamMemberVO vo = new TeamMemberVO();
            vo.setUserId(u.getId());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());

            Long orderCount = ordersMapper.selectCount(
                    new LambdaQueryWrapper<Orders>()
                            .eq(Orders::getUserId, u.getId())
            );
            vo.setOrderCount(orderCount.intValue());

            BigDecimal totalRebate = rebateRecordMapper.selectList(
                    new LambdaQueryWrapper<RebateRecord>()
                            .eq(RebateRecord::getUserId, u.getId())
                            .eq(RebateRecord::getInviterId, userId)
            ).stream().map(RebateRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTotalRebate(totalRebate);
            vo.setCreateTime(u.getCreateTime() != null ? u.getCreateTime().toString() : "");

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void requestWithdraw(WithdrawDTO dto) {
        Long userId = UserHolder.get();
        User user = userService.getById(userId);

        if (user.getBalance() == null || user.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("余额不足");
        }

        user.setBalance(user.getBalance().subtract(dto.getAmount()));
        userService.updateById(user);

        Withdrawal w = new Withdrawal();
        w.setUserId(userId);
        w.setAmount(dto.getAmount());
        w.setStatus(0);
        w.setAccountInfo(dto.getAccountInfo());
        withdrawalMapper.insert(w);
    }

    @Override
    @Transactional
    public void createRebateForOrder(Long orderId, Long userId, Long inviterId, BigDecimal amount) {
        RebateRecord record = new RebateRecord();
        record.setOrderId(orderId);
        record.setUserId(userId);
        record.setInviterId(inviterId);
        record.setAmount(amount);
        record.setStatus(1);
        rebateRecordMapper.insert(record);

        User inviter = userService.getById(inviterId);
        if (inviter != null) {
            inviter.setBalance(inviter.getBalance() != null ?
                    inviter.getBalance().add(amount) : amount);
            inviter.setTotalRebate(inviter.getTotalRebate() != null ?
                    inviter.getTotalRebate().add(amount) : amount);
            userService.updateById(inviter);
        }
    }
}
