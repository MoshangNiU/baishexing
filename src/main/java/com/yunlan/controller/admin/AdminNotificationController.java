package com.yunlan.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunlan.common.Result;
import com.yunlan.entity.Notification;
import com.yunlan.entity.User;
import com.yunlan.mapper.NotificationMapper;
import com.yunlan.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/notifications")
@Api(tags = "管理端 - 消息通知")
public class AdminNotificationController {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private UserMapper userMapper;

    @GetMapping("/page")
    @ApiOperation("通知分页")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer isRead) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(type)) w.eq(Notification::getType, type);
        if (isRead != null) w.eq(Notification::getIsRead, isRead);
        w.orderByDesc(Notification::getId);
        Page<Notification> p = notificationMapper.selectPage(new Page<>(page, pageSize), w);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @PostMapping("/push")
    @ApiOperation("推送消息")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> push(@RequestBody Map<String, Object> body) {
        String title = body.get("title") != null ? body.get("title").toString() : "";
        String content = body.get("content") != null ? body.get("content").toString() : "";
        String type = body.get("type") != null ? body.get("type").toString() : "system";
        String target = body.get("target") != null ? body.get("target").toString() : "all";

        if ("all".equals(target)) {
            List<User> users = userMapper.selectList(null);
            for (User u : users) {
                Notification n = new Notification();
                n.setUserId(u.getId());
                n.setTitle(title);
                n.setContent(content);
                n.setType(type);
                n.setIsRead(0);
                notificationMapper.insert(n);
            }
        } else if (body.get("userId") != null) {
            Long userId = Long.valueOf(body.get("userId").toString());
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setContent(content);
            n.setType(type);
            n.setIsRead(0);
            notificationMapper.insert(n);
        }
        return Result.success();
    }
}
