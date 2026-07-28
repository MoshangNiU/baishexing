package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.entity.Notification;
import com.yunlan.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@Api(tags = "消息通知模块")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/page")
    @ApiOperation("分页查询通知列表")
    public Result<List<Notification>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(notificationService.getNotificationPage(page, pageSize));
    }

    @PutMapping("/read/{id}")
    @ApiOperation("标记单条通知已读")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @ApiOperation("标记全部已读")
    public Result<Void> markAllRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @GetMapping("/unread-count")
    @ApiOperation("获取未读通知数")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }
}
