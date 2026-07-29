package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.SysLog;
import com.yunlan.mapper.SysLogMapper;
import com.yunlan.service.SysLogService;
import com.yunlan.utils.AdminUserHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    @Override
    @Async
    public void log(String module, String operation, String method, String params, String ip, Integer status) {
        SysLog log = new SysLog();
        log.setAdminId(AdminUserHolder.get());
        log.setModule(module);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setStatus(status != null ? status : 1);
        save(log);
    }

    @Override
    public Page<SysLog> getLogPage(int page, int pageSize, String module, String keyword) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq(SysLog::getModule, module);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysLog::getOperation, keyword)
                    .or().like(SysLog::getUsername, keyword);
        }
        wrapper.orderByDesc(SysLog::getId);
        return page(new Page<>(page, pageSize), wrapper);
    }
}
