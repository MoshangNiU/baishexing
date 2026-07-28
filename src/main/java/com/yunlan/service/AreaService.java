package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.Area;

import java.util.List;

public interface AreaService extends IService<Area> {
    List<Area> getChildren(Long parentId);
}
