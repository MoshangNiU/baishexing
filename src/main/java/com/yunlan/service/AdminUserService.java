package com.yunlan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.AdminUser;

import java.util.Map;

public interface AdminUserService extends IService<AdminUser> {
    Map<String, Object> login(String username, String password);
    AdminUser getCurrentAdmin();
    Page<AdminUser> getAdminPage(int page, int pageSize, String keyword);
    Long createAdmin(AdminUser adminUser);
    void updateAdmin(AdminUser adminUser);
    void deleteAdmin(Long id);
}
