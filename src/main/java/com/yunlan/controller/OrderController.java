package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.dto.CancelOrderDTO;
import com.yunlan.dto.OrderDetailVO;
import com.yunlan.dto.OrderVO;
import com.yunlan.dto.PayOrderDTO;
import com.yunlan.dto.PlaceOrderDTO;
import com.yunlan.entity.Orders;
import com.yunlan.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import com.yunlan.service.CouponService;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders-manager/consumer/orders")
@Api(tags = "订单模块")
public class OrderController {

    @Resource
    private OrdersService ordersService;

    @Resource
    private CouponService couponService;

    @PostMapping("/place")
    @ApiOperation("下单/预约服务")
    public Result<Orders> placeOrder(@RequestBody PlaceOrderDTO dto) {
        return Result.success(ordersService.placeOrder(dto));
    }

    @GetMapping("/page")
    @ApiOperation("获取订单列表（分页）")
    public Result<List<OrderVO>> page(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) Integer ordersStatus) {
        return Result.success(ordersService.getOrderPage(page, pageSize, ordersStatus));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取订单详情")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(ordersService.getOrderDetail(id));
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@RequestBody CancelOrderDTO dto) {
        ordersService.cancelOrder(dto);
        return Result.success();
    }

    @PutMapping("/hide/{id}")
    @ApiOperation("隐藏/删除订单")
    public Result<Void> hide(@PathVariable Long id) {
        ordersService.hideOrder(id);
        return Result.success();
    }

    @PutMapping("/pay/{id}")
    @ApiOperation("支付订单")
    public Result<Void> pay(@PathVariable Long id, @RequestBody PayOrderDTO dto) {
        ordersService.payOrder(id, dto);
        return Result.success();
    }

    @GetMapping("/pay/{id}/result")
    @ApiOperation("查询订单支付结果")
    public Result<Map<String, Object>> payResult(@PathVariable Long id) {
        return Result.success(ordersService.getPayResult(id));
    }

    @GetMapping("/consumerQueryList")
    @ApiOperation("订单滚动分页查询")
    public Result<List<OrderVO>> consumerQueryList(@RequestParam(required = false) Long lastId,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) Integer ordersStatus) {
        return Result.success(ordersService.consumerQueryList(lastId, pageSize, ordersStatus));
    }

    @GetMapping("/getAvailableCoupons")
    @ApiOperation("下单页可用优惠券列表")
    public Result<List<?>> getAvailableCoupons(@RequestParam Long orderId) {
        return Result.success(couponService.getAvailableCoupons(orderId));
    }
}
