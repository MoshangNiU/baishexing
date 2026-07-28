package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    List<Notification> getNotificationPage(int page, int pageSize);
    void markAsRead(Long id);
    void markAllAsRead();
    Long getUnreadCount();
    void createNotification(Long userId, String title, String content, String type, Long relatedOrderId);
}
