package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.Admin;
import com.gym.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录权限接口（B2）
 * 该 Controller 的 /login 被拦截器放行，其余接口仍需登录。
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AdminController {

    @Resource
    private AdminService adminService;

    /** 登录：POST /api/auth/login，请求体 {username, password} */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = adminService.login(body.get("username"), body.get("password"));
        return Result.success("登录成功", Map.of("token", token));
    }

    /** 退出：POST /api/auth/logout */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        adminService.logout(token);
        return Result.success("已退出", null);
    }

    /** 当前登录信息：GET /api/auth/info */
    @GetMapping("/info")
    public Result<Admin> info(@RequestHeader(value = "Authorization", required = false) String token) {
        return Result.success(adminService.getLoginAdmin(token));
    }
}
