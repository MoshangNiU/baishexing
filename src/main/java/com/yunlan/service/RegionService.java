package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.Region;

import java.util.List;
import java.util.Map;

public interface RegionService extends IService<Region> {
    List<Region> getActiveRegionList();
    Map<String, Object> queryRegionDisplayByCityCode(String cityCode);
    Map<String, Object> regeo(String location);
}
