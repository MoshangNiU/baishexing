package com.yunlan.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunlan.common.Result;
import com.yunlan.entity.ChatMessage;
import com.yunlan.entity.ChatSession;
import com.yunlan.entity.User;
import com.yunlan.mapper.ChatMessageMapper;
import com.yunlan.mapper.ChatSessionMapper;
import com.yunlan.mapper.UserMapper;
import com.yunlan.utils.AdminUserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/chat")
@Api(tags = "管理端 - 客服聊天")
public class AdminChatController {

    @Resource
    private ChatSessionMapper chatSessionMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private UserMapper userMapper;

    @GetMapping("/sessions")
    @ApiOperation("会话列表")
    public Result<List<ChatSession>> sessions(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ChatSession> w = new LambdaQueryWrapper<>();
        w.orderByDesc(ChatSession::getLastMessageTime);
        w.isNotNull(ChatSession::getLastMessageTime);
        List<ChatSession> list = chatSessionMapper.selectList(w);
        // 补充用户信息
        for (ChatSession s : list) {
            if (s.getUserId() != null) {
                User u = userMapper.selectById(s.getUserId());
                if (u != null) {
                    s.setUserNickname(u.getNickname());
                    s.setUserAvatar(u.getAvatar());
                    s.setUserPhone(u.getPhone());
                }
            }
        }
        return Result.success(list);
    }

    @GetMapping("/messages/{sessionId}")
    @ApiOperation("获取会话消息")
    public Result<Map<String, Object>> messages(@PathVariable Long sessionId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "50") int pageSize) {
        Long adminId = AdminUserHolder.get();
        if (adminId == null) return Result.noAuth();

        LambdaQueryWrapper<ChatMessage> w = new LambdaQueryWrapper<>();
        w.eq(ChatMessage::getSessionId, sessionId);
        w.orderByDesc(ChatMessage::getCreateTime);
        w.last("LIMIT " + (page - 1) * pageSize + ", " + pageSize);
        List<ChatMessage> list = chatMessageMapper.selectList(w);
        Collections.reverse(list);

        // 标记管理员端已读
        ChatMessage upd = new ChatMessage();
        upd.setIsRead(1);
        chatMessageMapper.update(upd, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getSenderType, 0)
                .eq(ChatMessage::getIsRead, 0));
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setAdminUnread(0);
            chatSessionMapper.updateById(session);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        if (session != null) {
            if (session.getUserId() != null) {
                User u = userMapper.selectById(session.getUserId());
                if (u != null) {
                    session.setUserNickname(u.getNickname());
                    session.setUserAvatar(u.getAvatar());
                    session.setUserPhone(u.getPhone());
                }
            }
        }
        result.put("session", session);
        return Result.success(result);
    }

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<ChatMessage> send(@RequestBody Map<String, Object> body) {
        Long adminId = AdminUserHolder.get();
        if (adminId == null) return Result.noAuth();
        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String content = body.get("content") != null ? body.get("content").toString().trim() : "";
        if (content.isEmpty()) return Result.error("消息内容不能为空");

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) return Result.error("会话不存在");

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(1);
        msg.setSenderId(adminId);
        msg.setContent(content);
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        session.setLastMessage(content);
        session.setLastMessageTime(LocalDateTime.now());
        session.setUserUnread((session.getUserUnread() == null ? 0 : session.getUserUnread()) + 1);
        chatSessionMapper.updateById(session);

        return Result.success(msg);
    }

    @GetMapping("/unread")
    @ApiOperation("获取管理员未读数")
    public Result<Integer> unread() {
        List<ChatSession> list = chatSessionMapper.selectList(null);
        int total = list.stream().mapToInt(s -> s.getAdminUnread() == null ? 0 : s.getAdminUnread()).sum();
        return Result.success(total);
    }
}
