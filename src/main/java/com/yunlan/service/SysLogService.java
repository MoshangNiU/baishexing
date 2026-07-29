package com.yunlan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.SysLog;

public interface SysLogService extends IService<SysLog> {
    void log(String module, String operation, String method, String params, String ip, Integer status);
    Page<SysLog> getLogPage(int page, int pageSize, String module, String keyword);
}
