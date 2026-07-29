package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.User;
import com.yunlan.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/users")
@Api(tags = "管理端 - 用户管理")
public class AdminUserController {

    @Resource
    private UserMapper userMapper;

    @GetMapping("/page")
    @ApiOperation("分页列表")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(User::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            w.and(ww -> ww.like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getInviteCode, keyword));
        }
        w.orderByDesc(User::getId);
        Page<User> p = userMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("用户详情")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }

    @PutMapping("/status")
    @ApiOperation("启用/冻结")
    public Result<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        User u = new User();
        u.setId(id);
        u.setStatus(status);
        userMapper.updateById(u);
        return Result.success();
    }
}
