package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.Trading;

import java.util.List;

public interface TradingService extends IService<Trading> {
    List<Trading> getTradingList(int page, int pageSize);
}
