package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.service.UserFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/consumer/favorite")
@Api(tags = "收藏模块")
public class FavoriteController {

    @Resource
    private UserFavoriteService userFavoriteService;

    @PostMapping("/toggle")
    @ApiOperation("切换收藏状态")
    public Result<Boolean> toggle(@RequestParam Long targetId, @RequestParam String targetType) {
        return Result.success(userFavoriteService.toggle(targetId, targetType));
    }

    @GetMapping("/list")
    @ApiOperation("获取我的收藏列表")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String targetType) {
        return Result.success(userFavoriteService.getMyFavorites(targetType));
    }

    @GetMapping("/check")
    @ApiOperation("检查是否已收藏")
    public Result<Boolean> check(@RequestParam Long targetId, @RequestParam String targetType) {
        return Result.success(userFavoriteService.isFavorite(targetId, targetType));
    }
}
