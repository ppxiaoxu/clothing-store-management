package com.xinrong.store.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinrong.store.common.Result;
import com.xinrong.store.entity.SysUser;
import com.xinrong.store.service.SysUserService;
import com.xinrong.store.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ADMIN_ROLE = "ADMIN";
    private static final int STATUS_ENABLED = 1;

    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;

    public JwtAuthInterceptor(JwtUtil jwtUtil, SysUserService sysUserService) {
        this.jwtUtil = jwtUtil;
        this.sysUserService = sysUserService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }

        String token = header.substring(7);
        try {
            Long userId = jwtUtil.parseUserId(token);
            SysUser user = sysUserService.getById(userId);
            if (user == null || !Integer.valueOf(STATUS_ENABLED).equals(user.getStatus())) {
                writeUnauthorized(response);
                return false;
            }
            if (isUserManageApi(request) && !ADMIN_ROLE.equals(user.getRole())) {
                writeForbidden(response);
                return false;
            }
            request.setAttribute("userId", userId);
            request.setAttribute("role", user.getRole());
            return true;
        } catch (Exception e) {
            writeUnauthorized(response);
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.error(401, "未登录或登录已过期")));
    }

    private void writeForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.error(403, "无权限访问")));
    }

    private boolean isUserManageApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.equals("/api/user") || path.startsWith("/api/user/");
    }
}
