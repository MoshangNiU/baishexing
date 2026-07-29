package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.entity.Trading;
import com.yunlan.service.TradingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/trade/consumer")
@Api(tags = "交易模块")
public class TradeController {

    @Resource
    private TradingService tradingService;

    @GetMapping("/tradings")
    @ApiOperation("查询交易单列表")
    public Result<List<Trading>> tradingList(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(tradingService.getTradingList(page, pageSize));
    }
}
