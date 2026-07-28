package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.CancelOrderDTO;
import com.yunlan.dto.OrderDetailVO;
import com.yunlan.dto.OrderVO;
import com.yunlan.dto.PayOrderDTO;
import com.yunlan.dto.PlaceOrderDTO;
import com.yunlan.entity.Orders;

import java.util.List;
import java.util.Map;

public interface OrdersService extends IService<Orders> {
    Orders placeOrder(PlaceOrderDTO dto);
    List<OrderVO> getOrderPage(int page, int pageSize, Integer ordersStatus);
    OrderDetailVO getOrderDetail(Long id);
    void cancelOrder(CancelOrderDTO dto);
    void hideOrder(Long id);
    void payOrder(Long id, PayOrderDTO dto);
    List<OrderVO> consumerQueryList(Long lastId, Integer pageSize, Integer ordersStatus);
    Map<String, Object> getPayResult(Long id);
    OrderVO convertToOrderVO(Orders order);
}
