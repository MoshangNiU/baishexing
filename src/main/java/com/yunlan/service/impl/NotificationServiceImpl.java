package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.Notification;
import com.yunlan.mapper.NotificationMapper;
import com.yunlan.service.NotificationService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public List<Notification> getNotificationPage(int page, int pageSize) {
        Long userId = UserHolder.get();
        if (userId == null) return List.of();

        int offset = (page - 1) * pageSize;
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime)
                .last("LIMIT " + offset + ", " + pageSize);

        return this.list(wrapper);
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = UserHolder.get();
        Notification notif = this.getById(id);
        if (notif != null && notif.getUserId().equals(userId)) {
            notif.setIsRead(1);
            this.updateById(notif);
        }
    }

    @Override
    public void markAllAsRead() {
        Long userId = UserHolder.get();
        if (userId == null) return;

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        List<Notification> unread = this.list(wrapper);
        for (Notification n : unread) {
            n.setIsRead(1);
        }
        this.updateBatchById(unread);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = UserHolder.get();
        if (userId == null) return 0L;

        return this.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .count();
    }

    @Override
    public void createNotification(Long userId, String title, String content, String type, Long relatedOrderId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRelatedOrderId(relatedOrderId);
        n.setIsRead(0);
        this.save(n);
    }
}
