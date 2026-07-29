package com.yunlan.controller.consumer;

import com.yunlan.common.Result;
import com.yunlan.dto.HistoryOrderDetailVO;
import com.yunlan.dto.OrderVO;
import com.yunlan.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/orders-history/consumer/orders")
@Api(tags = "历史订单模块")
public class HistoryOrderController {

    @Resource
    private OrdersService ordersService;

    @GetMapping("/list")
    @ApiOperation("获取历史订单列表")
    public Result<List<OrderVO>> list(@RequestParam(required = false) String minSortTime,
                                       @RequestParam(required = false) String maxSortTime,
                                       @RequestParam(required = false) String lastSortTime) {
        // 历史订单使用全部已完成的订单
        return Result.success(ordersService.getOrderPage(1, 10, 400));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取历史订单详情")
    public Result<HistoryOrderDetailVO> detail(@PathVariable Long id) {
        HistoryOrderDetailVO vo = new HistoryOrderDetailVO();
        OrderVO orderInfo = ordersService.convertToOrderVO(ordersService.getById(id));
        vo.setOrderInfo(orderInfo);
        vo.setRefundInfo(orderInfo);
        vo.setServerName(orderInfo.getServerName());
        vo.setOrdersStatus(orderInfo.getOrdersStatus());
        vo.setPayStatus(orderInfo.getPayStatus());
        return Result.success(vo);
    }
}
