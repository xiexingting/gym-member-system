-- 必须放在最前面：声明连接字符集为 utf8mb4，
-- 否则中文会按默认字符集（GBK）解析成乱码，导致 "Data too long for column" 报错
SET NAMES utf8mb4;

-- ============================================================
-- 健身房会员管理系统 —— 成员 A 模块数据库变更（完整可重复执行版）
-- 对应任务：A3 续费记录 / A4 状态管理 / A5 卡种套餐 / A6 到期提醒
--
-- 执行方式（在 gym_db 库上执行）：
--   mysql -uroot -p gym_db < backend/sql/schema_modules_a.sql
-- 或者在 Navicat / DataGrip 里打开本文件直接运行。
--
-- 本脚本可重复执行：
--   - CREATE TABLE IF NOT EXISTS 不会重复建表
--   - 加字段用存储过程判断，已存在则跳过
--   - INSERT 用 ON DUPLICATE KEY 不会重复插入示例卡种
-- ============================================================

-- 1. 会员续费记录表（原脚本遗漏，导致队友新库缺表、续费功能报错）
CREATE TABLE IF NOT EXISTS member_recharge (
  recharge_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '续费记录ID',
  member_id INT NOT NULL COMMENT '会员ID',
  recharge_money DECIMAL(10,2) NOT NULL COMMENT '续费金额',
  recharge_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '续费时间',
  add_month INT NOT NULL COMMENT '延长月数',
  card_id INT DEFAULT NULL COMMENT '关联卡种ID',
  operator VARCHAR(50) DEFAULT NULL COMMENT '操作人',
  INDEX idx_member_id (member_id),
  CONSTRAINT fk_recharge_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员续费记录表';

-- 2. 兼容旧库：如果 member_recharge 已存在但缺 card_id 字段，则补上
--    MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，所以用存储过程判断
DROP PROCEDURE IF EXISTS add_card_id_if_missing;
DELIMITER $$
CREATE PROCEDURE add_card_id_if_missing()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'member_recharge'
      AND COLUMN_NAME = 'card_id'
  ) THEN
    ALTER TABLE member_recharge
      ADD COLUMN card_id INT DEFAULT NULL COMMENT '关联卡种ID' AFTER add_month;
  END IF;
END$$
DELIMITER ;
CALL add_card_id_if_missing();
DROP PROCEDURE IF EXISTS add_card_id_if_missing;

-- 3. 卡种 / 套餐表
CREATE TABLE IF NOT EXISTS member_card_type (
  card_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '卡种ID',
  card_name VARCHAR(50) NOT NULL COMMENT '卡种名称：月卡/季卡/年卡/次卡',
  price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',
  valid_days INT DEFAULT NULL COMMENT '有效天数（时长卡用）',
  card_type VARCHAR(20) NOT NULL DEFAULT '时长卡' COMMENT '时长卡 / 次卡',
  total_times INT DEFAULT NULL COMMENT '总次数（次卡用）',
  remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=上架 0=下架',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_card_name (card_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡种套餐表';

-- 4. 到期提醒记录表
CREATE TABLE IF NOT EXISTS expiry_reminder (
  reminder_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '提醒ID',
  member_id INT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  expire_time DATETIME DEFAULT NULL COMMENT '会员到期时间',
  reminder_type TINYINT NOT NULL COMMENT '1=即将到期(7天内) 2=已到期',
  reminder_date DATE NOT NULL COMMENT '提醒日期',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未处理 1=已处理',
  remark VARCHAR(200) DEFAULT NULL COMMENT '处理备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_member_date (member_id, reminder_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='到期提醒记录表';

-- 5. 初始化示例卡种（可选，方便演示；已存在同名卡种时不会重复插入）
INSERT INTO member_card_type (card_name, price, valid_days, card_type, total_times, remark, status) VALUES
('月卡', 299.00, 30, '时长卡', NULL, '30 天有效', 1),
('季卡', 799.00, 90, '时长卡', NULL, '90 天有效', 1),
('年卡', 2599.00, 365, '时长卡', NULL, '365 天有效', 1),
('次卡(20次)', 999.00, NULL, '次卡', 20, '20 次有效', 1)
ON DUPLICATE KEY UPDATE card_name = card_name;

-- ============================================================
-- 会员状态取值约定（member.member_status）
--   1 = 正常
--   2 = 停用（手动设置，定时任务与续费都不会覆盖）
--   3 = 已到期（定时任务自动设置；续费后自动恢复为 1）
-- "即将到期"不单独存状态，由前端按到期时间实时计算（7 天内）。
-- ============================================================
