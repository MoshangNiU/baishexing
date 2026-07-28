package com.yunlan.service.impl;

import com.yunlan.dto.FreightDTO;
import com.yunlan.service.FreightService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FreightServiceImpl implements FreightService {

    @Override
    public BigDecimal calculate(FreightDTO dto) {
        // 模拟运费计算，实际根据距离、服务类型、城市等维度计算
        return new BigDecimal("0.00");
    }
}
