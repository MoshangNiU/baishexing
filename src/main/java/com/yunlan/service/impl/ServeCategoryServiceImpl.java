package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.ServeCategory;
import com.yunlan.mapper.ServeCategoryMapper;
import com.yunlan.service.ServeCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServeCategoryServiceImpl extends ServiceImpl<ServeCategoryMapper, ServeCategory> implements ServeCategoryService {

    @Override
    public List<ServeCategory> getFirstPageServeList() {
        LambdaQueryWrapper<ServeCategory> wrapper = new LambdaQueryWrapper<ServeCategory>()
                .eq(ServeCategory::getStatus, 1)
                .orderByAsc(ServeCategory::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<ServeCategory> getServeTypeList() {
        return this.list(new LambdaQueryWrapper<ServeCategory>()
                .eq(ServeCategory::getStatus, 1)
                .orderByAsc(ServeCategory::getSort));
    }
}
