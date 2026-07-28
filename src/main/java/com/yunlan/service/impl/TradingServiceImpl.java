package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.Trading;
import com.yunlan.mapper.TradingMapper;
import com.yunlan.service.TradingService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradingServiceImpl extends ServiceImpl<TradingMapper, Trading> implements TradingService {

    @Override
    public List<Trading> getTradingList(int page, int pageSize) {
        Long userId = UserHolder.get();
        return this.list(new LambdaQueryWrapper<Trading>()
                .eq(Trading::getUserId, userId)
                .orderByDesc(Trading::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize));
    }
}
