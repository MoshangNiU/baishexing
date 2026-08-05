package com.yunlan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long serveItemId;
    private String serveItemName;
    private String serveItemImage;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer userUnread;
    private Integer adminUnread;

    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String userPhone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
