package com.yunlan.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.AdminUser;
import com.yunlan.mapper.AdminUserMapper;
import com.yunlan.service.AdminUserService;
import com.yunlan.utils.AdminUserHolder;
import com.yunlan.utils.JwtUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Override
    public Map<String, Object> login(String username, String password) {
        AdminUser admin = lambdaQuery().eq(AdminUser::getUsername, username).one();
        if (admin == null) {
            throw new RuntimeException("账号或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }
        String token = JwtUtils.generateAdminToken(admin.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        result.put("avatar", admin.getAvatar());
        result.put("role", admin.getRole());
        return result;
    }

    @Override
    public AdminUser getCurrentAdmin() {
        Long adminId = AdminUserHolder.get();
        if (adminId == null) return null;
        return getById(adminId);
    }

    @Override
    public Page<AdminUser> getAdminPage(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AdminUser::getUsername, keyword)
                    .or().like(AdminUser::getNickname, keyword);
        }
        wrapper.orderByDesc(AdminUser::getId);
        return page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public Long createAdmin(AdminUser adminUser) {
        Long count = lambdaQuery().eq(AdminUser::getUsername, adminUser.getUsername()).count();
        if (count > 0) {
            throw new RuntimeException("账号已存在");
        }
        adminUser.setPassword(BCrypt.hashpw(adminUser.getPassword()));
        save(adminUser);
        return adminUser.getId();
    }

    @Override
    public void updateAdmin(AdminUser adminUser) {
        if (adminUser.getPassword() != null && !adminUser.getPassword().isEmpty()) {
            adminUser.setPassword(BCrypt.hashpw(adminUser.getPassword()));
        }
        updateById(adminUser);
    }

    @Override
    public void deleteAdmin(Long id) {
        if (id == 1L) {
            throw new RuntimeException("超级管理员不可删除");
        }
        removeById(id);
    }
}
