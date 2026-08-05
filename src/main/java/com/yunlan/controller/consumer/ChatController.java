package com.yunlan.controller.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunlan.common.Result;
import com.yunlan.entity.ChatMessage;
import com.yunlan.entity.ChatSession;
import com.yunlan.entity.ServeItem;
import com.yunlan.mapper.ChatMessageMapper;
import com.yunlan.mapper.ChatSessionMapper;
import com.yunlan.mapper.ServeItemMapper;
import com.yunlan.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/consumer/chat")
@Api(tags = "C端 - 客服聊天")
public class ChatController {

    @Resource
    private ChatSessionMapper chatSessionMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private ServeItemMapper serveItemMapper;

    @PostMapping("/session")
    @ApiOperation("获取或创建会话（基于服务ID）")
    public Result<ChatSession> getOrCreateSession(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        Long userId = UserHolder.get();
        if (userId == null) return Result.noAuth();
        Long serveItemId = body.get("serveItemId") != null ? Long.valueOf(body.get("serveItemId").toString()) : null;

        LambdaQueryWrapper<ChatSession> w = new LambdaQueryWrapper<>();
        w.eq(ChatSession::getUserId, userId);
        if (serveItemId != null) w.eq(ChatSession::getServeItemId, serveItemId);
        else w.isNull(ChatSession::getServeItemId);
        w.orderByDesc(ChatSession::getLastMessageTime);
        w.last("LIMIT 1");
        ChatSession session = chatSessionMapper.selectOne(w);

        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setServeItemId(serveItemId);
            session.setUserUnread(0);
            session.setAdminUnread(0);
            if (serveItemId != null) {
                ServeItem item = serveItemMapper.selectById(serveItemId);
                if (item != null) {
                    session.setServeItemName(item.getName());
                    session.setServeItemImage(item.getImage());
                }
            }
            chatSessionMapper.insert(session);
        }
        return Result.success(session);
    }

    @GetMapping("/sessions")
    @ApiOperation("我的会话列表")
    public Result<List<ChatSession>> mySessions() {
        Long userId = UserHolder.get();
        if (userId == null) return Result.noAuth();
        LambdaQueryWrapper<ChatSession> w = new LambdaQueryWrapper<>();
        w.eq(ChatSession::getUserId, userId);
        w.orderByDesc(ChatSession::getLastMessageTime);
        return Result.success(chatSessionMapper.selectList(w));
    }

    @GetMapping("/messages/{sessionId}")
    @ApiOperation("获取会话消息")
    public Result<Map<String, Object>> messages(@PathVariable Long sessionId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "30") int pageSize) {
        Long userId = UserHolder.get();
        if (userId == null) return Result.noAuth();
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) return Result.error("无权限");

        LambdaQueryWrapper<ChatMessage> w = new LambdaQueryWrapper<>();
        w.eq(ChatMessage::getSessionId, sessionId);
        w.orderByDesc(ChatMessage::getCreateTime);
        w.last("LIMIT " + (page - 1) * pageSize + ", " + pageSize);
        List<ChatMessage> list = chatMessageMapper.selectList(w);
        java.util.Collections.reverse(list);

        // 标记用户端已读
        ChatMessage upd = new ChatMessage();
        upd.setIsRead(1);
        chatMessageMapper.update(upd, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getSenderType, 1)
                .eq(ChatMessage::getIsRead, 0));
        session.setUserUnread(0);
        chatSessionMapper.updateById(session);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("session", session);
        return Result.success(result);
    }

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<ChatMessage> send(@RequestBody Map<String, Object> body) {
        Long userId = UserHolder.get();
        if (userId == null) return Result.noAuth();
        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String content = body.get("content") != null ? body.get("content").toString().trim() : "";
        if (content.isEmpty()) return Result.error("消息内容不能为空");

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) return Result.error("无权限");

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(0);
        msg.setSenderId(userId);
        msg.setContent(content);
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        session.setLastMessage(content);
        session.setLastMessageTime(LocalDateTime.now());
        session.setAdminUnread((session.getAdminUnread() == null ? 0 : session.getAdminUnread()) + 1);
        chatSessionMapper.updateById(session);

        return Result.success(msg);
    }

    @GetMapping("/unread")
    @ApiOperation("获取未读数")
    public Result<Integer> unread() {
        Long userId = UserHolder.get();
        if (userId == null) return Result.success(0);
        List<ChatSession> list = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>().eq(ChatSession::getUserId, userId));
        int total = list.stream().mapToInt(s -> s.getUserUnread() == null ? 0 : s.getUserUnread()).sum();
        return Result.success(total);
    }
}
