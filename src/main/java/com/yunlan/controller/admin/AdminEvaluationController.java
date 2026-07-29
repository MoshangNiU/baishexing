package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Evaluation;
import com.yunlan.entity.User;
import com.yunlan.entity.WorkerRecommend;
import com.yunlan.entity.ServeItem;
import com.yunlan.mapper.EvaluationMapper;
import com.yunlan.mapper.UserMapper;
import com.yunlan.mapper.WorkerRecommendMapper;
import com.yunlan.mapper.ServeItemMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-api/evaluations")
@Api(tags = "管理端 - 评价管理")
public class AdminEvaluationController {

    @Resource
    private EvaluationMapper evaluationMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private WorkerRecommendMapper workerRecommendMapper;
    @Resource
    private ServeItemMapper serveItemMapper;

    @GetMapping("/page")
    @ApiOperation("评价分页")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer star,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Evaluation> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Evaluation::getStatus, status);
        if (star != null) w.eq(Evaluation::getStar, star);
        w.orderByDesc(Evaluation::getId);

        if (StrUtil.isNotBlank(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getNickname, keyword));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            List<WorkerRecommend> workers = workerRecommendMapper.selectList(
                    new LambdaQueryWrapper<WorkerRecommend>().like(WorkerRecommend::getName, keyword));
            List<ServeItem> items = serveItemMapper.selectList(
                    new LambdaQueryWrapper<ServeItem>().like(ServeItem::getName, keyword));
            List<Long> itemIds = items.stream().map(ServeItem::getId).collect(Collectors.toList());

            w.and(ww -> {
                ww.like(Evaluation::getContent, keyword);
                if (!userIds.isEmpty()) ww.or().in(Evaluation::getUserId, userIds);
                if (!itemIds.isEmpty()) ww.or().in(Evaluation::getServeItemId, itemIds);
            });
        }

        Page<Evaluation> p = evaluationMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PutMapping("/status")
    @ApiOperation("显示/隐藏")
    public Result<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        Evaluation e = new Evaluation();
        e.setId(id);
        e.setStatus(status);
        evaluationMapper.updateById(e);
        return Result.success();
    }
}
