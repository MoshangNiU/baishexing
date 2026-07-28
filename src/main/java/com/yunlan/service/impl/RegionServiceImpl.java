package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.Region;
import com.yunlan.mapper.RegionMapper;
import com.yunlan.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    @Override
    public List<Region> getActiveRegionList() {
        return this.list(new LambdaQueryWrapper<Region>()
                .eq(Region::getActiveStatus, 1));
    }

    @Override
    public Map<String, Object> queryRegionDisplayByCityCode(String cityCode) {
        Region region = this.getOne(new LambdaQueryWrapper<Region>()
                .eq(Region::getCityCode, cityCode));
        Map<String, Object> result = new HashMap<>();
        if (region != null) {
            result.put("cityCode", region.getCityCode());
            result.put("cityName", region.getCityName());
            result.put("province", region.getProvince());
            result.put("provinceCode", region.getProvinceCode());
        }
        return result;
    }

    @Override
    public Map<String, Object> regeo(String location) {
        // 模拟根据经纬度反查地址，实际对接高德地图API
        Map<String, Object> result = new HashMap<>();
        result.put("province", "浙江省");
        result.put("city", "杭州市");
        result.put("district", "西湖区");
        result.put("cityCode", "330100");
        result.put("adcode", "330106");
        return result;
    }
}
