package com.yunlan.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.AdminUser;
import com.yunlan.entity.SysLog;
import com.yunlan.service.AdminUserService;
import com.yunlan.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/system")
@Api(tags = "管理端 - 系统管理")
public class AdminSystemController {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private SysLogService sysLogService;

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
}
