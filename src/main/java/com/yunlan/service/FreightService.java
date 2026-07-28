package com.yunlan.service;

import com.yunlan.dto.FreightDTO;

import java.math.BigDecimal;

public interface FreightService {
    BigDecimal calculate(FreightDTO dto);
}
