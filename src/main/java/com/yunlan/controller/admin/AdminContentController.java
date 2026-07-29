package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Banner;
import com.yunlan.entity.Region;
import com.yunlan.mapper.BannerMapper;
import com.yunlan.mapper.RegionMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin-api")
@Api(tags = "管理端 - 内容管理")
public class AdminContentController {

    @Resource
    private BannerMapper bannerMapper;
    @Resource
    private RegionMapper regionMapper;

    @GetMapping("/banners/page")
    @ApiOperation("Banner分页")
    public Result<Map<String, Object>> bannersPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Banner> p = bannerMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort));
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PostMapping("/banner")
    @ApiOperation("新增Banner")
    public Result<Long> addBanner(@RequestBody Banner banner) {
        bannerMapper.insert(banner);
        return Result.success(banner.getId());
    }

    @PutMapping("/banner")
    @ApiOperation("修改Banner")
    public Result<Void> updateBanner(@RequestBody Banner banner) {
        bannerMapper.updateById(banner);
        return Result.success();
    }

    @DeleteMapping("/banner/{id}")
    @ApiOperation("删除Banner")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/regions/page")
    @ApiOperation("开通城市分页")
    public Result<Map<String, Object>> regionsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Region> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) w.like(Region::getCityName, keyword);
        w.orderByDesc(Region::getId);
        Page<Region> p = regionMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PutMapping("/region/status")
    @ApiOperation("开通/关闭城市")
    public Result<Void> regionStatus(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer activeStatus = Integer.valueOf(body.get("activeStatus").toString());
        Region r = new Region();
        r.setId(id);
        r.setActiveStatus(activeStatus);
        regionMapper.updateById(r);
        return Result.success();
    }
}
