package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.ServeCategory;

import java.util.List;

public interface ServeCategoryService extends IService<ServeCategory> {
    List<ServeCategory> getFirstPageServeList();
    List<ServeCategory> getServeTypeList();
}
