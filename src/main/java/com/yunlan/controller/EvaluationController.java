package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.dto.EvaluationDTO;
import com.yunlan.dto.EvaluationVO;
import com.yunlan.dto.LikeDTO;
import com.yunlan.dto.UserReportDTO;
import com.yunlan.entity.Evaluation;
import com.yunlan.service.EvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/consumer/evaluation")
@Api(tags = "评价模块")
public class EvaluationController {

    @Resource
    private EvaluationService evaluationService;

    @GetMapping("/findAllSystemInfo")
    @ApiOperation("查询评价项信息")
    public Result<List<Map<String, Object>>> findAllSystemInfo() {
        return Result.success(evaluationService.findAllSystemInfo());
    }

    @PostMapping
    @ApiOperation("发表评价")
    public Result<Void> addEvaluation(@RequestBody EvaluationDTO dto) {
        evaluationService.addEvaluation(dto);
        return Result.success();
    }

    @GetMapping("/pageByCurrentUser")
    @ApiOperation("分页查询当前用户评价列表")
    public Result<List<Evaluation>> pageByCurrentUser(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(evaluationService.pageByCurrentUser(page, pageSize));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除评价")
    public Result<Void> deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return Result.success();
    }

    @GetMapping("/pageByTarget")
    @ApiOperation("根据对象属性分页查询评价列表")
    public Result<List<EvaluationVO>> pageByTarget(@RequestParam Long targetId,
                                                    @RequestParam(defaultValue = "1") int pageNo,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(evaluationService.pageByTarget(targetId, pageNo, pageSize, null));
    }

    @PostMapping("/likeOrCancel")
    @ApiOperation("点赞或取消点赞评价")
    public Result<Void> likeOrCancel(@RequestBody LikeDTO dto) {
        EvaluationDTO evalDto = new EvaluationDTO();
        evalDto.setEvaluationId(dto.getEvaluationId());
        evalDto.setLikeFlag(dto.getLikeFlag());
        evaluationService.likeOrCancel(evalDto);
        return Result.success();
    }

    @GetMapping("/countEvaluationByServeItemId")
    @ApiOperation("查询服务项的评价数")
    public Result<Integer> countEvaluation(@RequestParam Long serveItemId) {
        return Result.success(evaluationService.countEvaluationByServeItemId(serveItemId));
    }

    @PostMapping("/userReport")
    @ApiOperation("用户举报")
    public Result<Void> userReport(@RequestBody UserReportDTO dto) {
        evaluationService.userReport(dto);
        return Result.success();
    }
}
