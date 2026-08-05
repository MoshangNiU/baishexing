package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Evaluation;
import com.yunlan.entity.Orders;
import com.yunlan.entity.WorkerRecommend;
import com.yunlan.mapper.EvaluationMapper;
import com.yunlan.mapper.OrdersMapper;
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

@RestController
@RequestMapping("/admin-api/workers")
@Api(tags = "管理端 - 服务人员管理")
public class AdminWorkerController {

    @Resource
    private WorkerRecommendMapper workerRecommendMapper;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private EvaluationMapper evaluationMapper;

    private void enrichWorkerStats(WorkerRecommend w) {
        if (w == null || w.getId() == null) return;
        Long serveCount = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getWorkerId, w.getId())
                        .eq(Orders::getDeleted, 0));
        w.setServeCount(serveCount != null ? serveCount.intValue() : 0);

        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getWorkerId, w.getId())
                        .eq(Evaluation::getStatus, 1)
                        .eq(Evaluation::getDeleted, 0));
        if (evals != null && !evals.isEmpty()) {
            double avg = evals.stream().mapToInt(e -> e.getStar() != null ? e.getStar() : 0).average().orElse(0.0);
            w.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        } else {
            w.setRating(BigDecimal.valueOf(5.0));
        }
    }

    @GetMapping("/page")
    @ApiOperation("分页列表")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<WorkerRecommend> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(WorkerRecommend::getStatus, status);
        if (categoryId != null) w.eq(WorkerRecommend::getCategoryId, categoryId);
        if (StrUtil.isNotBlank(keyword)) {
            w.and(ww -> ww.like(WorkerRecommend::getName, keyword)
                    .or().like(WorkerRecommend::getPhone, keyword)
                    .or().like(WorkerRecommend::getSkills, keyword));
        }
        w.orderByDesc(WorkerRecommend::getId);
        Page<WorkerRecommend> p = workerRecommendMapper.selectPage(new Page<>(page, pageSize), w);
        p.getRecords().forEach(this::enrichWorkerStats);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<WorkerRecommend> detail(@PathVariable Long id) {
        WorkerRecommend w = workerRecommendMapper.selectById(id);
        enrichWorkerStats(w);
        return Result.success(w);
    }

    @PutMapping("/audit")
    @ApiOperation("审核")
    public Result<Void> audit(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        WorkerRecommend wr = new WorkerRecommend();
        wr.setId(id);
        wr.setStatus(status);
        workerRecommendMapper.updateById(wr);
        return Result.success();
    }

    @PutMapping("/status")
    @ApiOperation("启用/停用")
    public Result<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        WorkerRecommend wr = new WorkerRecommend();
        wr.setId(id);
        wr.setStatus(status);
        workerRecommendMapper.updateById(wr);
        return Result.success();
    }

    @PutMapping("/update")
    @ApiOperation("更新资料")
    public Result<Void> update(@RequestBody WorkerRecommend worker) {
        workerRecommendMapper.updateById(worker);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public Result<Void> delete(@PathVariable Long id) {
        workerRecommendMapper.deleteById(id);
        return Result.success();
    }
}
