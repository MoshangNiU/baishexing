package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.Area;
import com.yunlan.mapper.AreaMapper;
import com.yunlan.service.AreaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {

    @Override
    public List<Area> getChildren(Long parentId) {
        return this.list(new LambdaQueryWrapper<Area>()
                .eq(Area::getParentId, parentId)
                .orderByAsc(Area::getId));
    }
}
