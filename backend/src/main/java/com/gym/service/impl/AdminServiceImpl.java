package com.gym.service.impl;

import com.gym.common.TokenStore;
import com.gym.entity.Admin;
import com.gym.mapper.AdminMapper;
import com.gym.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private TokenStore tokenStore;

    @Override
    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("请输入账号");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("请输入密码");
        }

        Admin admin = adminMapper.selectByUsername(username.trim());
        if (admin == null || !admin.getPassword().equals(password)) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new IllegalArgumentException("该账号已停用，请联系管理员");
        }

        // 发出 token 前抹掉密码，避免密码跟着会话对象在外层流转
        admin.setPassword(null);
        return tokenStore.create(admin);
    }

    @Override
    public void logout(String token) {
        tokenStore.remove(token);
    }

    @Override
    public Admin getLoginAdmin(String token) {
        Admin admin = tokenStore.get(token);
        if (admin == null) {
            throw new IllegalArgumentException("登录已失效，请重新登录");
        }
        return admin;
    }
}
