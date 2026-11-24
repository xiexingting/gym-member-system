-- B2 登录权限：管理员表
-- 安全升级：密码改为 BCrypt 加密存储；新增 role 字段做权限分级
CREATE TABLE IF NOT EXISTS admin (
    admin_id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt 加密，形如 $2a$10$...）',
    real_name   VARCHAR(50) COMMENT '真实姓名',
    role        TINYINT      DEFAULT 1 COMMENT '角色：1=超级管理员(全部权限) 2=普通员工(只读，不能删除/改权限)',
    status      TINYINT      DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 兼容旧库：已存在但没有 role 字段的，补上（MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，用存储过程）
DROP PROCEDURE IF EXISTS add_admin_role_if_missing;
DELIMITER $$
CREATE PROCEDURE add_admin_role_if_missing()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin' AND COLUMN_NAME = 'role'
  ) THEN
    ALTER TABLE admin ADD COLUMN role TINYINT DEFAULT 1 COMMENT '角色：1超级管理员 2普通员工' AFTER real_name;
  END IF;
END$$
DELIMITER ;
CALL add_admin_role_if_missing();
DROP PROCEDURE IF EXISTS add_admin_role_if_missing;

-- 兼容旧库：把已有的明文密码 admin/123456 升级成 BCrypt 哈希（仅当仍是明文时）
UPDATE admin
SET password = '$2a$10$wm.s5I8oJjT/77e3CFqzwOErFk4jXAefslvU1gboyPNjmAQbykk92'
WHERE username = 'admin' AND password NOT LIKE '$2%';

-- 默认超级管理员：账号 admin / 密码 123456（BCrypt 哈希），拥有全部权限
INSERT INTO admin(username, password, real_name, role, status)
SELECT 'admin', '$2a$10$wm.s5I8oJjT/77e3CFqzwOErFk4jXAefslvU1gboyPNjmAQbykk92', '系统管理员', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admin WHERE username = 'admin');

-- 默认普通员工：账号 staff / 密码 123456，只读（不能删除、不能改权限）
INSERT INTO admin(username, password, real_name, role, status)
SELECT 'staff', '$2a$10$wm.s5I8oJjT/77e3CFqzwOErFk4jXAefslvU1gboyPNjmAQbykk92', '前台员工', 2, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admin WHERE username = 'staff');
