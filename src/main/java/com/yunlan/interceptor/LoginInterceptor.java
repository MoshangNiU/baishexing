package com.yunlan.interceptor;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunlan.common.Result;
import com.yunlan.utils.AdminUserHolder;
import com.yunlan.utils.JwtUtils;
import com.yunlan.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            writeUnauthorized(response);
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String role = JwtUtils.parseRole(token);
            Long subjectId = JwtUtils.parseToken(token);
            if (subjectId == null) {
                writeUnauthorized(response);
                return false;
            }
            String uri = request.getRequestURI();
            if (JwtUtils.ROLE_ADMIN.equals(role)) {
                if (!uri.startsWith("/admin-api/")) {
                    writeForbidden(response, "管理员Token不可访问C端接口");
                    return false;
                }
                AdminUserHolder.set(subjectId);
            } else {
                if (uri.startsWith("/admin-api/")) {
                    writeForbidden(response, "用户Token不可访问管理端接口");
                    return false;
                }
                UserHolder.set(subjectId);
            }
            return true;
        } catch (Exception e) {
            log.error("JWT token解析失败: {}", token, e);
            writeUnauthorized(response);
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.noAuth()));
    }

    private void writeForbidden(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.error(403, msg)));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.remove();
        AdminUserHolder.remove();
    }
}
