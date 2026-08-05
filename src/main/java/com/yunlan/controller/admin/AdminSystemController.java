package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.AdminUser;
import com.yunlan.entity.SysConfig;
import com.yunlan.entity.SysLog;
import com.yunlan.mapper.SysConfigMapper;
import com.yunlan.service.AdminUserService;
import com.yunlan.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-api/system")
@Api(tags = "管理端 - 系统管理")
public class AdminSystemController {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private SysLogService sysLogService;
    @Resource
    private SysConfigMapper sysConfigMapper;

    @GetMapping("/accounts/page")
    @ApiOperation("管理员分页")
    public Result<Map<String, Object>> accountsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<AdminUser> p = adminUserService.getAdminPage(page, pageSize, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PostMapping("/account")
    @ApiOperation("新增管理员")
    public Result<Long> addAccount(@RequestBody AdminUser adminUser) {
        return Result.success(adminUserService.createAdmin(adminUser));
    }

    @PutMapping("/account")
    @ApiOperation("修改管理员")
    public Result<Void> updateAccount(@RequestBody AdminUser adminUser) {
        adminUserService.updateAdmin(adminUser);
        return Result.success();
    }

    @DeleteMapping("/account/{id}")
    @ApiOperation("删除管理员")
    public Result<Void> deleteAccount(@PathVariable Long id) {
        adminUserService.deleteAdmin(id);
        return Result.success();
    }

    @GetMapping("/logs/page")
    @ApiOperation("操作日志分页")
    public Result<Map<String, Object>> logsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String keyword) {
        Page<SysLog> p = sysLogService.getLogPage(page, pageSize, module, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    // ========== 系统配置管理 ==========

    @GetMapping("/config/list")
    @ApiOperation("系统配置列表")
    public Result<List<SysConfig>> configList(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysConfig> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            w.and(ww -> ww.like(SysConfig::getConfigKey, keyword)
                    .or().like(SysConfig::getConfigDesc, keyword));
        }
        w.orderByAsc(SysConfig::getId);
        return Result.success(sysConfigMapper.selectList(w));
    }

    @PostMapping("/config")
    @ApiOperation("新增系统配置")
    public Result<Long> addConfig(@RequestBody SysConfig config) {
        sysConfigMapper.insert(config);
        return Result.success(config.getId());
    }

    @PutMapping("/config")
    @ApiOperation("修改系统配置")
    public Result<Void> updateConfig(@RequestBody SysConfig config) {
        sysConfigMapper.updateById(config);
        return Result.success();
    }

    @PutMapping("/config/batch")
    @ApiOperation("批量保存系统配置")
    public Result<Void> batchSaveConfig(@RequestBody List<SysConfig> list) {
        for (SysConfig c : list) {
            if (c.getId() != null) {
                sysConfigMapper.updateById(c);
            } else {
                sysConfigMapper.insert(c);
            }
        }
        return Result.success();
    }

    @DeleteMapping("/config/{id}")
    @ApiOperation("删除系统配置")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        sysConfigMapper.deleteById(id);
        return Result.success();
    }
}
