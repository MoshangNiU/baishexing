package com.yunlan.service;

import com.yunlan.dto.DistributionInfoVO;
import com.yunlan.dto.RebateRecordVO;
import com.yunlan.dto.TeamMemberVO;
import com.yunlan.dto.WithdrawDTO;

import java.util.List;

public interface DistributionService {
    DistributionInfoVO getMyDistributionInfo();
    List<RebateRecordVO> getRebateRecords(int page, int pageSize);
    List<TeamMemberVO> getTeamMembers(int page, int pageSize);
    void requestWithdraw(WithdrawDTO dto);
    void createRebateForOrder(Long orderId, Long userId, Long inviterId, java.math.BigDecimal amount);
}
