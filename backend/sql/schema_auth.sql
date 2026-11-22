-- B2 登录权限：管理员表
CREATE TABLE IF NOT EXISTS admin (
    admin_id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（课程项目暂为明文，生产环境应加密存储）',
    real_name   VARCHAR(50) COMMENT '真实姓名',
    status      TINYINT      DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 默认管理员：账号 admin / 密码 123456
INSERT INTO admin(username, password, real_name, status)
SELECT 'admin', '123456', '系统管理员', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admin WHERE username = 'admin');
