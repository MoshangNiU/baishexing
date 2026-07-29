package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.WorkerRecommend;
import com.yunlan.mapper.WorkerRecommendMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/workers")
@Api(tags = "管理端 - 服务人员管理")
public class AdminWorkerController {

    @Resource
    private WorkerRecommendMapper workerRecommendMapper;

    @GetMapping("/page")
    @ApiOperation("分页列表")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<WorkerRecommend> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(WorkerRecommend::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            w.and(ww -> ww.like(WorkerRecommend::getName, keyword)
                    .or().like(WorkerRecommend::getSkills, keyword));
        }
        w.orderByDesc(WorkerRecommend::getId);
        Page<WorkerRecommend> p = workerRecommendMapper.selectPage(new Page<>(page, pageSize), w);
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
        return Result.success(workerRecommendMapper.selectById(id));
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
