package com.gym.common;

import com.gym.entity.Admin;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 未登录拦截器：
 * 所有 /api/** 请求（除登录接口外）都要带有效的 Authorization 头，
 * 否则直接返回 401，请求不会进到 Controller。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private TokenStore tokenStore;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 放行 CORS 预检请求（浏览器跨域带自定义头时会先发 OPTIONS，不带 token）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        Admin admin = tokenStore.get(token);

        if (admin == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, 401,
                    "未登录或登录已失效，请重新登录");
            return false;
        }

        // 权限分级：role=2 普通员工只读，禁止所有写操作（POST/PUT/DELETE）。
        // role 为空或 1 一律按超级管理员处理，兼容旧库升级的账号。
        if (admin.getRole() != null && admin.getRole() == 2
                && !"GET".equalsIgnoreCase(request.getMethod())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, 403,
                    "当前账号为只读角色，无权执行此操作，请联系管理员");
            return false;
        }
        return true;
    }

    /** 直接写 JSON，避免为拦截器引入额外依赖 */
    private void writeJson(HttpServletResponse response, int httpStatus, int code, String message)
            throws Exception {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}"
        );
    }
}
