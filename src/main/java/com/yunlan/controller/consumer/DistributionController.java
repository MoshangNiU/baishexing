package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.dto.DistributionInfoVO;
import com.yunlan.dto.RebateRecordVO;
import com.yunlan.dto.TeamMemberVO;
import com.yunlan.dto.WithdrawDTO;
import com.yunlan.service.DistributionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/distribution")
@Api(tags = "分销模块")
public class DistributionController {

    @Resource
    private DistributionService distributionService;

    @GetMapping("/my-info")
    @ApiOperation("获取分销中心信息")
    public Result<DistributionInfoVO> getMyInfo() {
        return Result.success(distributionService.getMyDistributionInfo());
    }

    @GetMapping("/rebate-records")
    @ApiOperation("返利记录分页查询")
    public Result<List<RebateRecordVO>> getRebateRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(distributionService.getRebateRecords(page, pageSize));
    }

    @GetMapping("/team")
    @ApiOperation("团队成员列表")
    public Result<List<TeamMemberVO>> getTeam(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(distributionService.getTeamMembers(page, pageSize));
    }

    @PostMapping("/withdraw")
    @ApiOperation("提现申请")
    public Result<Void> withdraw(@RequestBody WithdrawDTO dto) {
        distributionService.requestWithdraw(dto);
        return Result.success();
    }
}
