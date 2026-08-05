package com.yunlan.controller.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunlan.common.Result;
import com.yunlan.entity.SysConfig;
import com.yunlan.mapper.SysConfigMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/foundations/consumer/config")
@Api(tags = "系统配置（公开接口）")
public class ConfigController {

    @Resource
    private SysConfigMapper sysConfigMapper;

    @GetMapping("/all")
    @ApiOperation("获取所有公开配置（返回 key-value Map）")
    public Result<Map<String, String>> getAll() {
        List<SysConfig> list = sysConfigMapper.selectList(
                new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getId));
        Map<String, String> result = new HashMap<>();
        for (SysConfig c : list) {
            result.put(c.getConfigKey(), c.getConfigValue());
        }
        return Result.success(result);
    }

    @GetMapping("/{key}")
    @ApiOperation("根据 key 获取单个配置")
    public Result<String> getByKey(@PathVariable String key) {
        SysConfig c = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return Result.success(c != null ? c.getConfigValue() : null);
    }
}
