package com.gym.service;

import com.gym.entity.Admin;

public interface AdminService {

    /** 登录校验，成功返回 token，失败抛异常 */
    String login(String username, String password);

    /** 退出登录 */
    void logout(String token);

    /** 按 token 获取当前登录管理员 */
    Admin getLoginAdmin(String token);
}
