package com.gym.common;

import com.gym.entity.Admin;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存令牌库（课程项目版）：
 * 登录成功后发一个随机 token，服务端记住 token 对应哪个管理员。
 * 不依赖 Redis 等外部组件，重启后端后所有 token 失效，需要重新登录。
 */
@Component
public class TokenStore {

    /** token 有效期：2 小时（毫秒），每次访问自动续期 */
    private static final long EXPIRE_MILLIS = 2 * 60 * 60 * 1000L;

    /** 一条登录会话：管理员 + 到期时间戳 */
    private static class LoginSession {
        Admin admin;
        long expireAt;

        LoginSession(Admin admin, long expireAt) {
            this.admin = admin;
            this.expireAt = expireAt;
        }
    }

    private final Map<String, LoginSession> sessionMap = new ConcurrentHashMap<>();

    /** 创建 token 并登记管理员 */
    public String create(Admin admin) {
        cleanExpired();
        String token = UUID.randomUUID().toString().replace("-", "");
        sessionMap.put(token, new LoginSession(admin, System.currentTimeMillis() + EXPIRE_MILLIS));
        return token;
    }

    /** 按 token 取管理员，过期或不存在返回 null；有效则自动续期 */
    public Admin get(String token) {
        if (token == null) {
            return null;
        }
        LoginSession session = sessionMap.get(token);
        if (session == null) {
            return null;
        }
        if (System.currentTimeMillis() > session.expireAt) {
            sessionMap.remove(token);
            return null;
        }
        session.expireAt = System.currentTimeMillis() + EXPIRE_MILLIS;
        return session.admin;
    }

    /** 退出登录，移除 token */
    public void remove(String token) {
        if (token != null) {
            sessionMap.remove(token);
        }
    }

    /** 顺手清掉所有过期会话，避免 Map 一直膨胀 */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        sessionMap.entrySet().removeIf(entry -> now > entry.getValue().expireAt);
    }
}
