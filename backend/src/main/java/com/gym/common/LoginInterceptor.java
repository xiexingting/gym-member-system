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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            // 结构固定，直接手写 JSON，避免额外依赖
            response.getWriter().write(
                    "{\"code\":401,\"message\":\"未登录或登录已失效，请重新登录\",\"data\":null}"
            );
            return false;
        }
        return true;
    }
}
