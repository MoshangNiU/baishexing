package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.ServeCategory;
import com.yunlan.entity.ServeItem;
import com.yunlan.mapper.ServeCategoryMapper;
import com.yunlan.mapper.ServeItemMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/serve")
@Api(tags = "管理端 - 服务管理")
public class AdminServeController {

    @Resource
    private ServeCategoryMapper serveCategoryMapper;
    @Resource
    private ServeItemMapper serveItemMapper;

    @GetMapping("/categories")
    @ApiOperation("分类列表")
    public Result<List<ServeCategory>> categories() {
        return Result.success(serveCategoryMapper.selectList(
                new LambdaQueryWrapper<ServeCategory>().orderByAsc(ServeCategory::getSort)));
    }

    @PostMapping("/category")
    @ApiOperation("新增分类")
    public Result<Long> addCategory(@RequestBody ServeCategory category) {
        serveCategoryMapper.insert(category);
        return Result.success(category.getId());
    }

    @PutMapping("/category")
    @ApiOperation("修改分类")
    public Result<Void> updateCategory(@RequestBody ServeCategory category) {
        serveCategoryMapper.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    @ApiOperation("删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        serveCategoryMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/items/page")
    @ApiOperation("服务项目分页")
    public Result<Map<String, Object>> itemsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ServeItem> w = new LambdaQueryWrapper<>();
        if (categoryId != null) w.eq(ServeItem::getCategoryId, categoryId);
        if (status != null) w.eq(ServeItem::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) w.like(ServeItem::getName, keyword);
        w.orderByDesc(ServeItem::getId);
        Page<ServeItem> p = serveItemMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PostMapping("/item")
    @ApiOperation("新增服务项目")
    public Result<Long> addItem(@RequestBody ServeItem item) {
        serveItemMapper.insert(item);
        return Result.success(item.getId());
    }

    @PutMapping("/item")
    @ApiOperation("修改服务项目")
    public Result<Void> updateItem(@RequestBody ServeItem item) {
        serveItemMapper.updateById(item);
        return Result.success();
    }

    @DeleteMapping("/item/{id}")
    @ApiOperation("删除服务项目")
    public Result<Void> deleteItem(@PathVariable Long id) {
        serveItemMapper.deleteById(id);
        return Result.success();
    }
}
